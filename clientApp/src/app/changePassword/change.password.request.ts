export class ChangePasswordRequest{
    #oldpassword: string;
    #newPassword: string;
    #confirmationPassword: string;
    
    constructor(oldPassword: string,newPassword: string, confirmationPassword: string){
        this.#oldpassword = oldPassword;
        this.#newPassword = newPassword;
        this.#confirmationPassword = confirmationPassword;
    }

    getOldPassword(): string{
       return this.#oldpassword;
    }

    setOldPassword(oldPassword: string): void{
        this.#oldpassword = oldPassword;
    }

    getNewPassword(): string{
        return this.#newPassword
    }

    setNewPassword(newPassword: string): void{
        this.#newPassword = newPassword;
    }
    
    getConfirmationPassword(): string{
        return this.#confirmationPassword
    }

    setConfirmationPassword(confirmationPassword: string): void{
        this.#confirmationPassword = confirmationPassword;
    }

}