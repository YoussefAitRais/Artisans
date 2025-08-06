import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import {RouterLink} from "@angular/router";

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginObj = {
    email: '',
    password: ''
  };

  onLoginSubmit(form: any) {
    if (form.valid) {
      console.log('Login Data:', this.loginObj);
      // TODO: call backend service
    } else {
      alert('Please fill in all fields');
    }
  }
}
