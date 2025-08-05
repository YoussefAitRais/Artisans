import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {NavbarComponent} from "./navbar/navbar.component";
import {HomeComponent} from "./home/home.component";
import {CardsComponent} from "./cards/cards.component";
import {WhyUsComponent} from "./why-us/why-us.component";
import {WhyChooseUsComponent} from "./why-choose-us/why-choose-us.component";
import {FaqComponent} from "./faq/faq.component";
import {FooterComponent} from "./footer/footer.component";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    NavbarComponent,
    HomeComponent,
    CardsComponent,
    WhyUsComponent,
    WhyChooseUsComponent,
    FaqComponent,
    FooterComponent
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'frontend';
}
