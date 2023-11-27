import {parseRole, Role} from "../user/Role";
import {parseEnum} from "@angular/compiler-cli/linker/src/file_linker/partial_linkers/util";
import {Observable} from "rxjs";

export class CurrentUserResponse {

    #userId: string;
    #firstname: string;
    #lastname: string;
    #email: string;
    #specialization: string;
    #currentProject: string;
    #groupName: string;
    #role: Role | undefined;

  constructor(
    userId: string,
    firstname: string,
    lastname: string,
    email: string,
    specialization: string,
    currentProject: string,
    groupName: string,
    role: string
  ) {
    this.#userId = userId;
    this.#firstname = firstname;
    this.#lastname = lastname;
    this.#email = email;
    this.#specialization = specialization;
    this.#currentProject = currentProject;
    this.#groupName = groupName;
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


  toJSON(): any {
    return {
      userId: this.userId,
      firstname: this.firstname,
      lastname: this.lastname,
      email: this.email,
      specialization: this.specialization,
      currentProject: this.currentProject,
      groupName: this.groupName,
      role: this.role
    };
  }
}

function getCurrentUser():CurrentUserResponse{
  const currentUser = localStorage.getItem('ConnectedUserDetails')!;
  let parsedCurrentUser = JSON.parse(currentUser);
  return new CurrentUserResponse(
    parsedCurrentUser.userId,
    parsedCurrentUser.firstname,
    parsedCurrentUser.lastname,
    parsedCurrentUser.email,
    parsedCurrentUser.specialization,
    parsedCurrentUser.currentProject,
    parsedCurrentUser.groupName,
    parsedCurrentUser.role
  );
}
export {getCurrentUser};
