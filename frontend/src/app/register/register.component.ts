import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

export type registerForm = {
  firstName: string,
  lastName: string,
  email: string,
  password: string,
  confirmerPassword: string,
  role: string
};

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
})
export class RegisterComponent {
  registerObj: registerForm = {
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmerPassword: '',
    role: ''
  };

  onRegisterSubmit(form: any) {
    if (!this.registerObj.role) {
      alert('Please select a role.');
      return;
    }
    console.log(this.registerObj);
  }

  setRole(role: string) {
    this.registerObj.role = role;
  }
}


