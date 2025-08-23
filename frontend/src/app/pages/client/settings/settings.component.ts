import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClientApi } from '../../../services/api/client-api.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html'
})
export class SettingsComponent implements OnInit {
  name = signal('');
  email = signal('');
  phone = signal('');
  city = signal(''); // ماعندناش city فـ Client حالياً، خليه UI فقط

  currentPwd = signal('');
  newPwd = signal('');
  confirmPwd = signal('');

  constructor(private api: ClientApi) {}

  ngOnInit(): void {
    this.api.getMe().subscribe(me => {
      this.name.set(`${me.nom}${me.prenom ? ' ' + me.prenom : ''}`);
      this.email.set(me.email);
      this.phone.set(me.telephone || '');
    });
  }

  saveProfile() {
    const [nom, ...rest] = this.name().split(' ');
    const prenom = rest.join(' ');
    this.api.updateMe({ nom: nom || '', prenom, telephone: this.phone() })
      .subscribe(() => alert('Profil mis à jour ✔️'));
  }

  changePassword() {
    // ماعندناش endpoint لتغيير الباسورد في الباك الآن.
    alert('TODO: ajouter endpoint /api/client/change-password');
  }

  deleteAccount() { alert('TODO: supprimer compte'); }
}
