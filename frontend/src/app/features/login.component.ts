import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../core/auth.service';

@Component({ selector:'cc-login', standalone:true, imports:[CommonModule,FormsModule], templateUrl:'./login.component.html', styleUrl:'./login.component.css' })
export class LoginComponent {
  readonly auth=inject(AuthService); readonly signupMode=signal(false);
  readonly accessRoles=[
    { short:'AD', title:'Admin', detail:'Governance & audit', tone:'violet' },
    { short:'DR', title:'Doctor', detail:'Clinical workspace', tone:'blue' },
    { short:'RN', title:'Nurse', detail:'Care coordination', tone:'amber' },
    { short:'PT', title:'Patient', detail:'Personal care portal', tone:'mint' }
  ];
  username=''; password=''; firstName=''; lastName=''; email=''; phone=''; dateOfBirth='';
  get signupPasswordTooShort(){ return this.signupMode() && this.password.length > 0 && this.password.length < 8; }
  get signupPasswordHint(){ return this.password.length ? `${this.password.length} / 8 characters` : 'Use at least 8 characters.'; }
  submit(){ if(this.signupMode()) this.auth.signup({username:this.username,password:this.password,firstName:this.firstName,lastName:this.lastName,email:this.email,phone:this.phone,dateOfBirth:this.dateOfBirth}); else this.auth.login(this.username,this.password); }
  toggle(){ this.auth.error.set(''); this.signupMode.update(value=>!value); }
}
