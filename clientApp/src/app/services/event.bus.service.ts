import {EventEmitter, Injectable} from "@angular/core";
import {BehaviorSubject, Observable, Subject} from "rxjs";

@Injectable({
  providedIn: "root",
})

export class EventBusService {
  private loginSuccessSubject = new Subject<void>();

  triggerLoginSuccess() {
    this.loginSuccessSubject.next();
  }

  getLoginSuccessEvent() {
    return this.loginSuccessSubject.asObservable();
  }
}
