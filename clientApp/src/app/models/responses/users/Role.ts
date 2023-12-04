export enum Role {
  USER = 'USER',
  MANAGER = 'MANAGER',
  HR = 'HR',
  ADMIN = 'ADMIN'
}

function parseRole(roleString: string): Role | undefined {
  switch (roleString) {
    case 'ADMIN':
      return Role.ADMIN;
    case 'HR':
      return Role.HR;
    case 'MANAGER':
      return Role.MANAGER;
    case 'USER':
      return Role.USER;
    default:
      return undefined;
  }

}

export {parseRole};

