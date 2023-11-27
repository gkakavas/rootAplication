import { HttpStatusCode } from "@angular/common/http";

export class ServerErrorResponse {
    
  constructor(private _message:string, private _status: HttpStatusCode | null) {
  }
  
    get message(){
      return this._message
    }
    get status() {
      return this._status;
    }
}