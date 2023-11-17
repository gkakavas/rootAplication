export class LoginRequest {
  #username: string;
  #password: string;

  constructor(username: string, password: string) {
    this.#username = username;
    this.#password = password;
  }

  getUsername(): string {
    return this.#username;
  } 
  setUsername(username: string): void {
    this.#username = username;
  }
  getPassword(): string {
    return this.#password;
  }
  setPassword(password: string): void {
    this.#password = password;
  }
}