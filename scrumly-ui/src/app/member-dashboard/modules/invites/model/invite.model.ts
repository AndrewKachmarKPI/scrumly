import {
  OrganizationInfoDto, TeamDto
} from "../../organizations/model/organization.model";
import { UserProfileDto } from "../../../../auth/auth.model";

export interface InviteDto {
  id?: number;
  inviteId?: string;
  inviteUrl?: string;
  connectingId?: string;
  inviteType?: InviteType;
  currentStatus?: InviteStatus;
  createBy?: UserProfileDto;
  createdFor?: UserProfileDto;
  created?: string;
  accepted?: string;
  expiresAt?: string;
  isExpired?: boolean,
  changeLog: InviteHistoryDto[];
  orgInfoDto?: OrganizationInfoDto;
  teamInfo?: TeamDto;
  hasManagePermission: boolean
}

export enum InviteType {
  ALL="ALL",
  ORGANIZATION = "ORGANIZATION",
  TEAM = "TEAM"
}


export enum InviteStatus {
  NEW = "NEW",
  REJECTED = "REJECTED",
  ACCEPTED = "ACCEPTED",
  REVOKED = "REVOKED",
  RESENT = "RESENT",
  EXPIRED = "EXPIRED"
}


export interface InviteHistoryDto {
  id?: number;
  dateTime?: string;
  performedBy?: string;
  previousStatus?: InviteStatus;
  newStatus?: InviteStatus;
  changeAction?: InviteChangeAction;
}


export enum InviteChangeAction {
  MEMBER_CREATED = "MEMBER_CREATED",
  MEMBER_INVITED = "MEMBER_INVITED",
  MEMBER_JOINED = "MEMBER_JOINED",
  MEMBER_BLOCKED = "MEMBER_BLOCKED",
  MEMBER_ACTIVATED = "MEMBER_ACTIVATED",
  MEMBER_LEFT = "MEMBER_LEFT"
}

export interface InviteMembersRQ {
  usernames: string[];
}
