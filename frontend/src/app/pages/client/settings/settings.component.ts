import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html'
})
export class SettingsComponent {
  name = signal('Youssef');
  email = signal('client@example.com');
  phone = signal('+212 600 00 00 00');
  city = signal('Casablanca');

  // password
  currentPwd = signal('');
  newPwd = signal('');
  confirmPwd = signal('');

  saveProfile() {
    alert('Profil mis à jour ✔️');
  }

  changePassword() {
    if (!this.newPwd() || this.newPwd() !== this.confirmPwd()) {
      alert('Mot de passe non valide');
      return;
    }
    // call API here later
    alert('Mot de passe changé ✔️');
    this.currentPwd.set(''); this.newPwd.set(''); this.confirmPwd.set('');
  }

  deleteAccount() {
    if (confirm('Êtes-vous sûr de vouloir supprimer le compte ?')) {
      alert('Compte supprimé (mock)'); // TODO: appeler API
    }
  }
}
