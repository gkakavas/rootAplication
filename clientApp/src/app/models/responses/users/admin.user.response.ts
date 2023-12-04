import {parseRole, Role} from "./Role";

export class AdminUserResponse{
  #userId: string;
  #firstname: string;
  #lastname: string;
  #email: string;
  #specialization: string;
  #currentProject: string;
  #groupName: string;
  #createdBy: string | undefined;
  #registerDate: Date;
  #lastLogin: Date | undefined;
  #role: Role | undefined;

  constructor(
    userId: string,
    firstname: string,
    lastname: string,
    email: string,
    specialization: string,
    currentProject: string,
    groupName: string,
    createdBy: string | undefined,
    registerDate: string [],
    lastLogin: string [] | undefined,
    role: string,
  ) {
    this.#userId = userId;
    this.#firstname = firstname;
    this.#lastname = lastname;
    this.#email = email;
    this.#specialization = specialization;
    this.#currentProject = currentProject;
    this.#groupName = groupName;
    this.#createdBy = createdBy!;
    this.#registerDate = new Date(
      Number(registerDate[0]),
      Number(registerDate[1]),
      Number(registerDate[2]),
      Number(registerDate[3]),
      Number(registerDate[4]),
      Number(registerDate[5])
    )
    if(lastLogin){
      this.#lastLogin = new Date(
        Number(lastLogin[0]),
        Number(lastLogin[1]),
        Number(lastLogin[2]),
        Number(lastLogin[3]),
        Number(lastLogin[4]),
        Number(lastLogin[5])
      )
    }
    this.#role = parseRole(role);
  }

  get userId(): string {
    return this.#userId;
  }

  set userId(value: string) {
    this.#userId = value;
  }

  get firstname(): string {
    return this.#firstname;
  }

  set firstname(value: string) {
    this.#firstname = value;
  }

  get lastname(): string {
    return this.#lastname;
  }

  set lastname(value: string) {
    this.#lastname = value;
  }

  get email(): string {
    return this.#email;
  }

  set email(value: string) {
    this.#email = value;
  }

  get specialization(): string {
    return this.#specialization;
  }

  set specialization(value: string) {
    this.#specialization = value;
  }

  get currentProject(): string {
    return this.#currentProject;
  }

  set currentProject(value: string) {
    this.#currentProject = value;
  }

  get groupName(): string {
    return this.#groupName;
  }

  set groupName(value: string) {
    this.#groupName = value;
  }

  get role(): Role {
    return <Role>this.#role;
  }

  set role(value: string) {
    this.#role = parseRole(value);
  }

  get createdBy(): string | undefined {
    return this.#createdBy;
  }

  set createdBy(value: string | undefined) {
    this.#createdBy = value;
  }

  get registerDate(): Date {
    return this.#registerDate;
  }

  set registerDate(value: Date) {
    this.#registerDate = value;
  }

  get lastLogin(): Date | undefined {
    return this.#lastLogin;
  }

  set lastLogin(value: Date | undefined) {
    this.#lastLogin = value;
  }

}
