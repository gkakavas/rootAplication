import { CommonModule } from "@angular/common";
import { Component, OnInit } from "@angular/core";
import { RouterOutlet } from "@angular/router";
import { MatIconModule } from '@angular/material/icon';

@Component({
    standalone: true,
    selector: 'user-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css'],
    imports: [RouterOutlet,CommonModule, MatIconModule]
})


export class DashboardComponent implements OnInit{
    ngOnInit(): void {
        
    }
}