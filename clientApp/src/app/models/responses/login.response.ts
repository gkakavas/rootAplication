export class AuthenticationResponse {
    #token: string;
    constructor(token:string) {
      this.#token = token;
    }
    get token(): string{
      return this.#token;
    }
}
