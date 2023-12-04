import {parseRole, Role} from "./Role";

export class CurrentUserResponse {

    #userId: string;
    #firstname: string;
    #lastname: string;
    #email: string;
    #specialization: string;
    #currentProject: string;
    #groupName: string;
    #role: Role | undefined;
    #authorities: string[] = [];

  constructor(
    userId: string,
    firstname: string,
    lastname: string,
    email: string,
    specialization: string,
    currentProject: string,
    groupName: string,
    role: string,
    authorities: string []
  ) {
    this.#userId = userId;
    this.#firstname = firstname;
    this.#lastname = lastname;
    this.#email = email;
    this.#specialization = specialization;
    this.#currentProject = currentProject;
    this.#groupName = groupName;
    this.#role = parseRole(role);
    try {
      for (const authority of authorities) {
        this.#authorities.push(authority);
      }
    } catch (error) {
      console.error('Error parsing authorities JSON:', error);
      this.#authorities = [];
    }
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

  get authorities(): string []  {
    return this.#authorities;
  }

  set authorities(authorities :string []) {
    this.#authorities = authorities;
  }
}

