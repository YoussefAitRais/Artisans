import { Component } from '@angular/core';
import {NavbarComponent} from "../navbar/navbar.component";
import {CardsComponent} from "../cards/cards.component";
import {FaqComponent} from "../faq/faq.component";
import {FooterComponent} from "../footer/footer.component";
import {WhyChooseUsComponent} from "../why-choose-us/why-choose-us.component";
import {WhyUsComponent} from "../why-us/why-us.component";

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NavbarComponent,
    CardsComponent,
    FaqComponent,
    FooterComponent,
    WhyChooseUsComponent,
    WhyUsComponent
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

}
