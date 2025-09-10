import { Component, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink
  ],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  // Mobile menu state
  isMenuOpen = signal(false);

  // Toggle mobile menu
  toggleMenu() {
    this.isMenuOpen.update(value => !value);
  }

  // Close menu when clicking on a link
  closeMenu() {
    this.isMenuOpen.set(false);
  }

  // Close menu when pressing Escape key
  @HostListener('document:keydown.escape', ['$event'])
  onEscapeKey(event: KeyboardEvent) {
    if (this.isMenuOpen()) {
      this.closeMenu();
    }
  }

  // Close menu when window is resized to desktop size
  @HostListener('window:resize', ['$event'])
  onResize(event: any) {
    if (event.target.innerWidth >= 1024 && this.isMenuOpen()) {
      this.closeMenu();
    }
  }
}
