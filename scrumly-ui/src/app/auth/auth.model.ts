export interface RegisterUserRQ {
  email: string,
  firstName: string,
  lastName: string,
  password: string,
  dateOfBirth: string
}

export interface UserProfileDto {
  id: number;
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  bio: string;
  phoneNumber: string;
  registered: string;
  avatarId: string;
  lastActivity: string;
  dateOfBirth: string;
  skills: string[],
  connectedServices: UserServiceConnectionDto[],
  connectedOrganizations: OrganizationConnectionDto[]
}

export interface OrganizationConnectionDto {
  id: number,
  organizationId: string,
  isActive: boolean,
  dateConnected: string
}

export interface UserServiceConnectionDto {
  id: number,
  serviceName: string,
  dateConnected: string
}


export interface UserInfoDto {
  userId?: string;
  email?: string;
  firstName: string;
  lastName: string;
  avatarId?: string;
}


export interface UserProfileRQ {
  firstName: string,
  lastName: string,
  bio: string,
  phoneNumber: string,
  avatarFile: Blob,
  dateOfBirth: string,
  isRemoveAvatar: boolean,
  skills: string[]
}
