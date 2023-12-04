import {Component, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import { CommonModule } from '@angular/common';
import {ActivatedRoute, Router, RouterOutlet, UrlSegment} from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { LoginComponent } from "./login/login.component";
import {EventBusService} from "./services/event.bus.service";

@Component({
    selector: 'app-root',
    standalone: true,
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
    imports: [CommonModule, RouterOutlet, MatIconModule, LoginComponent]
})
export class AppComponent implements OnInit, OnChanges{
  title = 'clientApp';
  mainPage: any = {};

  constructor(
    private eventBusService: EventBusService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
  ) {
  }

  ngOnInit(){
    this.router.navigate(['login']);
    console.log(this.activatedRoute.snapshot);
  }
  ngAfterViewInit() {
    this.eventBusService.getLoginSuccessEvent().subscribe(() => {
      this.onSuccessfulLogin();
    });
  }


  private onSuccessfulLogin() {
    this.router.navigate(['dashboard','my-profile']);
  }

  ngOnChanges(changes:SimpleChanges): void {

    /*if(outlet.includes('login')){
      this.mainPage = {
        'display': 'flex',
        'justify-content': 'center',
        'align-items': 'center',
        'flex-direction': 'column',
        'width': '100%',
        'height': '100%',
      };
    }
    else {
      this.mainPage = {
        'display': 'flex',
        'flex-direction': 'column',
        'width': '100%',
        'height': '100%',
      };
    }*/
  }
}
