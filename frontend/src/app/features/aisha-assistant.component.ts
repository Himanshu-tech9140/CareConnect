import { CommonModule } from '@angular/common';
import { Component, Input, OnDestroy, signal } from '@angular/core';
import { DashboardSummary } from '../core/models';

interface SpeechRecognitionResultLike { [index: number]: { transcript: string }; }
interface SpeechRecognitionEventLike { results: { length: number; [index: number]: SpeechRecognitionResultLike }; }
interface SpeechRecognitionLike {
  lang: string;
  interimResults: boolean;
  continuous: boolean;
  onresult: ((event: SpeechRecognitionEventLike) => void) | null;
  onend: (() => void) | null;
  start(): void;
  stop(): void;
}

@Component({
  selector: 'cc-aisha-assistant',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './aisha-assistant.component.html',
  styleUrl: './aisha-assistant.component.css'
})
export class AishaAssistantComponent implements OnDestroy {
  @Input({ required: true }) summary!: DashboardSummary;
  @Input() isPatient = false;

  readonly listening = signal(false);
  readonly voiceLevel = signal(0);
  readonly response = signal('I have your workspace ready. What would you like to review?');
  readonly transcript = signal('');

  private audioContext: AudioContext | null = null;
  private analyser: AnalyserNode | null = null;
  private stream: MediaStream | null = null;
  private audioFrame = 0;
  private recognition: SpeechRecognitionLike | null = null;

  get greeting() { return this.isPatient ? 'Hi, I am Aisha' : 'Aisha is ready'; }

  ask(topic: 'brief' | 'appointment' | 'tasks' | 'risk') {
    const appointment = this.summary.nextAppointment;
    if (topic === 'appointment') {
      this.response.set(appointment ? `${appointment.type} is next on ${appointment.date} at ${appointment.time} with ${appointment.provider}.` : 'There are no upcoming appointments on this workspace.');
      return;
    }
    if (topic === 'tasks') {
      const openTasks = this.summary.tasks.reduce((total, task) => total + task.count, 0);
      this.response.set(openTasks ? `You have ${openTasks} attention item${openTasks === 1 ? '' : 's'} across the workspace.` : 'There are no outstanding attention items right now.');
      return;
    }
    if (topic === 'risk') {
      const highRiskPatients = this.summary.recentPatients.filter(patient => patient.risk === 'HIGH').length;
      this.response.set(highRiskPatients ? `${highRiskPatients} recent patient${highRiskPatients === 1 ? ' is' : 's are'} marked high risk. Open the patient records to review their latest chart details.` : 'No high-risk patients are present in the recent activity list.');
      return;
    }
    this.response.set(this.isPatient ? `Your dashboard shows ${this.summary.metrics[0]?.value ?? 0} upcoming visits and ${this.summary.metrics[1]?.value ?? 0} active medications.` : `${this.summary.metrics[0]?.value ?? 0} patients are in the workspace. I can help you review the next appointment or attention items.`);
  }

  async toggleListening() {
    if (this.listening()) {
      this.stopListening();
      return;
    }
    await this.startListening();
  }

  private async startListening() {
    if (!navigator.mediaDevices?.getUserMedia) {
      this.response.set('Microphone access is not available in this browser.');
      return;
    }
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.audioContext = new AudioContext();
      this.analyser = this.audioContext.createAnalyser();
      this.analyser.fftSize = 256;
      this.audioContext.createMediaStreamSource(this.stream).connect(this.analyser);
      this.listening.set(true);
      this.response.set('I am listening. Ask about your brief, appointment, or tasks.');
      this.startVoiceMeter();
      this.startSpeechRecognition();
    } catch {
      this.response.set('Microphone permission was not granted. You can still use the quick prompts.');
      this.stopListening();
    }
  }

  private startVoiceMeter() {
    if (!this.analyser) return;
    const values = new Uint8Array(this.analyser.frequencyBinCount);
    const measure = () => {
      if (!this.analyser || !this.listening()) return;
      this.analyser.getByteFrequencyData(values);
      const average = values.reduce((sum, value) => sum + value, 0) / values.length / 255;
      this.voiceLevel.set(Math.min(average * 2.6, 1));
      this.audioFrame = requestAnimationFrame(measure);
    };
    measure();
  }

  private startSpeechRecognition() {
    const recognitionConstructor = (window as Window & { webkitSpeechRecognition?: new () => SpeechRecognitionLike; SpeechRecognition?: new () => SpeechRecognitionLike }).SpeechRecognition
      ?? (window as Window & { webkitSpeechRecognition?: new () => SpeechRecognitionLike }).webkitSpeechRecognition;
    if (!recognitionConstructor) return;
    this.recognition = new recognitionConstructor();
    this.recognition.lang = 'en-US';
    this.recognition.interimResults = false;
    this.recognition.continuous = true;
    this.recognition.onresult = (event) => {
      const text = event.results[event.results.length - 1][0].transcript.trim();
      this.transcript.set(text);
      const request = text.toLowerCase();
      if (request.includes('appointment')) this.ask('appointment');
      else if (request.includes('task') || request.includes('attention')) this.ask('tasks');
      else if (request.includes('risk')) this.ask('risk');
      else this.ask('brief');
    };
    this.recognition.onend = () => { if (this.listening()) this.recognition?.start(); };
    this.recognition.start();
  }

  private stopListening() {
    cancelAnimationFrame(this.audioFrame);
    this.recognition?.stop();
    this.recognition = null;
    this.stream?.getTracks().forEach(track => track.stop());
    this.stream = null;
    void this.audioContext?.close();
    this.audioContext = null;
    this.analyser = null;
    this.voiceLevel.set(0);
    this.listening.set(false);
  }

  ngOnDestroy() { this.stopListening(); }
}