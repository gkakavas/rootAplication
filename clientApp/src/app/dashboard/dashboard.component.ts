import {Component, OnInit} from "@angular/core";
import {Router, RouterLink, RouterOutlet} from "@angular/router";
import {ReactiveFormsModule} from "@angular/forms";
import {CommonModule} from "@angular/common";

@Component({
  standalone: true,
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
  imports: [RouterOutlet, ReactiveFormsModule, CommonModule, RouterLink]
})
export class Dashboard implements OnInit{

  constructor(private router:Router) {
  }

  ngOnInit(): void {
  }

}
