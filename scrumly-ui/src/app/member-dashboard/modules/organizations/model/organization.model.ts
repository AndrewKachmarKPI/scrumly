import { UserInfoDto, UserProfileDto } from "../../../../auth/auth.model";
import { InviteDto } from "../../invites/model/invite.model";

export interface OrganizationInfoDto {
  organizationId: string;
  name: string;
  about?: string;
  logo: string;
  status?: OrganizationStatus;
  isActive?: boolean;
  created?: string;
  createdBy?: UserInfoDto;
  numberOfTeams?: number;
  numberOfMembers?: number;
  isRemoveLogo?: boolean,
  isOrgAccessBlocked?: boolean,
  orgMember: OrganizationMemberDto
}

export enum OrganizationStatus {
  ACTIVE = "ACTIVE",
  BLOCKED = "BLOCKED",
  INACTIVE = "INACTIVE",
  ARCHIVED = "ARCHIVED"
}

export interface CreateOrganizationRQ {
  organizationName: string;
  teamName?: string;
  inviteMembers?: string[];
}

export interface OrganizationInfoRQ {
  name: string;
  about?: string;
  status?: string[];
}

export interface OrganizationDto {
  id?: number;
  organizationId: string;
  name?: string;
  about?: string;
  logo?: string;
  status?: OrganizationStatus;
  isActive?: boolean;
  created?: string;
  createdBy?: UserProfileDto;
  teams?: TeamDto[];
  members?: OrganizationMemberDto[];
  changeHistory?: OrganizationHistoryDto[];
}

export interface TeamDto {
  id?: number;
  organizationId: string,
  teamId?: string;
  name?: string;
  created?: string;
  createdBy?: UserProfileDto;
  totalMembers?: number,
  members?: TeamMemberDto[];
}

export interface OrganizationTeamGroupDto {
  organization: OrganizationDto,
  teams: TeamDto[]
}


export interface OrganizationMemberDto {
  id?: number;
  role?: OrganizationMemberRole;
  joinDateTime?: string;
  status?: MemberStatus;
  profile?: UserProfileDto;
  invite?: InviteDto;
  changeHistory?: MemberHistoryDto[];
}

export interface MemberHistoryDto {
  dateTime?: string;
  performedBy?: string;
  previousStatus?: MemberStatus;
  newStatus?: MemberStatus;
  changeAction?: MemberChangeAction;
}

export enum MemberStatus {
  PENDING = "PENDING",
  ACTIVE = "ACTIVE",
  BLOCKED = "BLOCKED",
  LEFT = "LEFT"
}

export interface OrganizationHistoryDto {
  id?: number;
  dateTime?: string;
  performedBy?: string;
  previousStatus?: OrganizationStatus;
  newStatus?: OrganizationStatus;
  changeAction?: OrganizationChangeAction;
}

export interface TeamMemberDto {
  id?: number;
  role?: TeamMemberRole;
  joinDateTime?: string;
  status?: MemberStatus;
  badge?: string,
  profile?: UserProfileDto;
  changeHistory?: MemberHistoryDto[];
}

export enum TeamMemberRole {
  MEMBER = "MEMBER",
  SCRUM_MASTER = "SCRUM_MASTER",
  TEAM_LEAD = "TEAM_LEAD",
  TEAM_ADMIN = "TEAM_ADMIN"
}

export enum OrganizationMemberRole {
  MEMBER = "MEMBER",
  ORGANIZATION_LEAD = "ORGANIZATION_LEAD",
  ORGANIZATION_ADMIN = "ORGANIZATION_ADMIN"
}

export enum MemberChangeAction {
  MEMBER_CREATED = "MEMBER_CREATED",
  MEMBER_INVITED = "MEMBER_INVITED",
  MEMBER_JOINED = "MEMBER_JOINED",
  MEMBER_BLOCKED = "MEMBER_BLOCKED",
  MEMBER_ACTIVATED = "MEMBER_ACTIVATED",
  MEMBER_LEFT = "MEMBER_LEFT",
  MEMBER_ROLE_CHANGED = "MEMBER_ROLE_CHANGED"
}

export enum OrganizationChangeAction {
  CREATED = "CREATED",
  MEMBER_JOINED = "MEMBER_JOINED",
  TEAM_CREATED = "TEAM_CREATED",
  BLOCKED = "BLOCKED",
  UPDATED = "UPDATED"
}


export interface CreateTeamRQ {
  organizationId: string,
  teamName: string,
  inviteMembers: string[]
}

export interface UpdateTeamRQ {
  teamName?: string,
  updateMembers?: UpdateTeamMemberRQ[]
}

export interface UpdateTeamMemberRQ {
  userId: string,
  memberRole: TeamMemberRole,
  badge: string
}
