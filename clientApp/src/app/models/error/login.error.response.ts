export class LoginErrorResponse{
    constructor(private _message:string){
    }

    set message (message: string){
        this._message = message;
    }

    get message(){
        return this._message;
    }
}