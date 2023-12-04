
export class OtherUserResponse{
  #userId: string;
  #firstname: string;
  #lastname: string;
  #email: string;
  #specialization: string;
  #currentProject: string;
  #groupName: string;


  constructor(
    userId: string,
    firstname: string,
    lastname: string,
    email: string,
    specialization: string,
    currentProject: string,
    groupName: string,
  ) {
    this.#userId = userId;
    this.#firstname = firstname;
    this.#lastname = lastname;
    this.#email = email;
    this.#specialization = specialization;
    this.#currentProject = currentProject;
    this.#groupName = groupName;
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
}
