import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { MenuItem, MessageService } from 'primeng/api';
import { NavigationEnd, Router } from '@angular/router';
import { Menu } from 'primeng/menu';
import { filter, Subject, takeUntil } from 'rxjs';
import {
  OrganizationGroupSelection,
  OrganizationService
} from '../../modules/organizations/services/organization.service';
import { EventService } from '../../modules/events/services/event.service';
import { trackByLabel } from '../../../ui-components/services/utils';
import { BacklogService } from '../../modules/backlog/services/backlog.service';
import { BacklogDto } from '../../modules/backlog/model/backlog.model';


@Component({
  selector: 'member-dashboard-sidebar',
  templateUrl: './member-dashboard-sidebar.component.html',
  styleUrl: './member-dashboard-sidebar.component.css'
})
export class MemberDashboardSidebarComponent implements OnInit, OnDestroy {
  @Input() isSideBarOpened: boolean = false;
  @Output() isSideBarOpenedChange = new EventEmitter<boolean>();
  public menuItems: MenuItem[] | undefined;
  public customMenuItems = {
    ORG_LIST: {
      id: 'ORG_LIST'
    },
    ORG_INFO: {
      id: 'ORG_INFO',
      routerLink: '/app/org/dashboard'
    },
    SCHEDULE_ACTIVITY: {
      id: 'SCHEDULE_ACTIVITY',
      routerLink: '/app/activity/search'
    },
    CREATE_TEMPLATE: {
      id: 'CREATE_TEMPLATE',
      routerLink: '/app/activity/create'
    },
    BACKLOG_NAME: {
      id: 'BACKLOG_NAME'
    }
  }
  public isIconsMenuOpened: boolean = true;

  private destroy$ = new Subject<void>();
  private selectedGroup?: OrganizationGroupSelection;
  public selectedBacklog?: BacklogDto;


  constructor(private router: Router,
              private eventService: EventService,
              private messageService: MessageService,
              private organizationService: OrganizationService,
              private backlogService: BacklogService) {
  }

  ngOnInit() {
    this.organizationService.onOrganizationSelectionChange
      .pipe(takeUntil(this.destroy$), filter(Boolean))
      .subscribe(selection => {
        this.selectedGroup = selection;
        this.menuItems = this.getMenuItems();
        if (this.selectedBacklog && this.selectedGroup.team?.teamId !== this.selectedBacklog?.teamId) {
          this.selectedBacklog = undefined;
          this.getMenuItems();
          this.backlogService.selectBacklog(undefined);
        }
      });
    this.backlogService.onBacklogSelection
      .pipe(takeUntil(this.destroy$), filter(Boolean))
      .subscribe(selectedBacklog => {
        this.selectedBacklog = selectedBacklog;
        this.menuItems = this.getMenuItems();
      });
    this.router.events.pipe(
      takeUntil(this.destroy$),
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.menuItems = this.getMenuItems();
    });
    this.menuItems = this.getMenuItems();
  }

  getMenuItems() {
    return [
      {
        label: 'Home',
        icon: 'pi pi-home',
        routerLink: '/app/home',
        items: [
          {
            label: 'Events dashboard',
            icon: 'pi pi-calendar',
            routerLink: `/app/home/events`
          },
        ]
      },
      {
        separator: true
      },
      {
        label: 'Events',
        icon: 'pi pi-calendar',
        routerLink: `/app/events/meetings`,
        items: [
          {
            id: this.customMenuItems.SCHEDULE_ACTIVITY.id
          },
          {
            label: 'My events',
            icon: 'pi pi-user',
            routerLink: `/app/events/meetings`
          },
          {
            label: 'Team events',
            icon: 'pi pi-users',
            routerLink: `/app/events/${ (this.getTeamId()) }/meetings`,
            disabled: this.hasTeamSelected(),
          }
        ]
      },
      {
        label: 'Templates',
        icon: 'pi pi-objects-column',
        routerLink: '/app/activity',
        items: [
          {
            id: this.customMenuItems.CREATE_TEMPLATE.id
          },
          {
            label: 'Public library',
            icon: 'pi pi-objects-column',
            routerLink: '/app/activity/search'
          },
          {
            label: 'My templates',
            icon: 'pi pi-bookmark',
            routerLink: '/app/activity/my-templates'
          },
          {
            label: 'Team templates',
            icon: 'pi pi-users',
            routerLink: `/app/activity/${ (this.getTeamId()) }/team`,
            disabled: this.hasTeamSelected(),
          },
          {
            label: 'Template constructor',
            icon: 'pi pi-box',
            routerLink: '/app/activity/create'
          }
        ]
      },
      {
        label: 'Backlog',
        icon: 'pi pi-list',
        routerLink: this.hasBacklogSelected()
          ? `/app/backlog/${ (this.getTeamId()) }/list`
          : `/app/backlog/issues`,
        queryParams: {
          backlogId: this.getBacklogId()
        },
        disabled: this.hasTeamSelected(),
        items: [
          {
            label: 'Backlogs',
            icon: 'pi pi-list',
            routerLink: `/app/backlog/${ (this.getTeamId()) }/list`,
            disabled: this.hasTeamSelected(),
          },
          {
            id: this.customMenuItems.BACKLOG_NAME.id
          },
          {
            label: 'Issues',
            icon: 'pi pi-file',
            routerLink: `/app/backlog/issues`,
            queryParams: {
              backlogId: this.getBacklogId()
            },
            disabled: this.hasBacklogSelected(),
          },
          {
            label: 'Issues types',
            icon: 'pi pi-bolt',
            routerLink: `/app/backlog/issue-types`,
            queryParams: {
              backlogId: this.getBacklogId()
            },
            disabled: this.hasBacklogSelected(),
          },
          {
            label: 'Issues statuses',
            icon: 'pi pi-asterisk',
            routerLink: `/app/backlog/issue-statuses`,
            queryParams: {
              backlogId: this.getBacklogId()
            },
            disabled: this.hasBacklogSelected(),
          },
          {
            routerLink: `/issue`,
          },
        ]
      },
      {
        separator: true
      },
      {
        label: 'Organizations',
        icon: 'pi pi-building-columns',
        routerLink: '/app/org',
        items: [
          {
            label: 'My Organizations',
            icon: 'pi pi-building-columns',
            routerLink: '/app/org/list'
          },
          {
            id: this.customMenuItems.ORG_LIST.id
          },
          {
            label: 'Dashboard',
            icon: 'pi pi-home',
            routerLink: `/app/org/${ (this.getOrganizationId()) }/dashboard`,
            disabled: this.isOrganizationSelected(),
          },
          {
            label: 'Teams',
            icon: 'pi pi-sitemap',
            routerLink: `/app/org/${ (this.getOrganizationId()) }/teams`,
            disabled: this.isOrganizationSelected(),
          },
          {
            label: 'Members',
            icon: 'pi pi-users',
            routerLink: `/app/org/${ (this.getOrganizationId()) }/members`,
            disabled: this.isOrganizationSelected(),
          },
          {
            label: 'Invites',
            icon: 'pi pi-share-alt',
            routerLink: `/app/org/${ (this.getOrganizationId()) }/invites`,
            disabled: this.isOrganizationSelected(),
          },
          {
            label: 'Integrations',
            icon: 'pi pi-th-large',
            routerLink: `/app/org/${ (this.getOrganizationId()) }/integrations`,
            disabled: this.isOrganizationSelected(),
          },
          {
            id: 'SETTINGS',
            label: 'Settings',
            icon: 'pi pi-cog',
            routerLink: `/app/org/${ (this.getOrganizationId()) }/settings`,
            disabled: this.isOrganizationSelected(),
          },
        ]
      },
      {
        label: 'Teams',
        icon: 'pi pi-users',
        routerLink: !this.hasTeamSelected()
          ? `/app/teams/${ (this.getTeamId()) }/dashboard`
          : `/app/teams/my`,
        items: [
          {
            label: 'Team dashboard',
            icon: 'pi pi-users',
            routerLink: `/app/teams/${ (this.getTeamId()) }/dashboard`,
            disabled: this.hasTeamSelected(),
          },
          {
            label: 'My teams',
            icon: 'pi pi-users',
            routerLink: `/app/teams/my`,
          }
        ]
      },
      {
        separator: true
      },
      {
        label: 'Settings',
        icon: 'pi pi-cog',
        routerLink: '/app/settings',
        items: [
          {
            label: 'Profile',
            icon: 'pi pi-user',
            routerLink: '/app/settings/profile'
          },
          {
            label: 'Pending Invites',
            icon: 'pi pi-share-alt',
            routerLink: '/app/settings/invites'
          },
          {
            label: 'Personal Integrations',
            icon: 'pi pi-th-large',
            routerLink: '/app/settings/integrations'
          }
        ]
      }
    ];
  }


  private getTeamId(): string {
    return this.selectedGroup?.team?.teamId!;
  }

  private hasTeamSelected() {
    return !this.selectedGroup || !this.selectedGroup?.team;
  }

  private getOrganizationId(): string {
    return this.selectedGroup?.org?.organizationId!;
  }

  private isOrganizationSelected() {
    return !this.selectedGroup || !this.selectedGroup?.org;
  }

  private getBacklogId(): string {
    return this.selectedBacklog?.backlogId!;
  }

  private hasBacklogSelected() {
    return !this.selectedBacklog;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  isActiveTab(item: MenuItem) {
    let links = [ item.routerLink ];
    if (item.items && item.items.length > 0) {
      links.push(...item.items.map(value => value.routerLink));
    }
    return links.some(value => this.router.url.includes(value));
  }

  toggleIconMenu() {
    this.isIconsMenuOpened = !this.isIconsMenuOpened;
  }

  toggleSideBarMenu() {
    this.isIconsMenuOpened = true;
    this.isSideBarOpened = !this.isSideBarOpened;
    this.isSideBarOpenedChange.emit(this.isSideBarOpened);
  }

  openIconMenu() {
    this.isIconsMenuOpened = true;
  }

  onIconItemClick(item: MenuItem, event: Event) {
    if (item.disabled) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Select organization team',
        detail: `You cannot proceed without selecting team`,
        key: 'sidebar'
      });
      return;
    }
    if (item.routerLink) {
      this.router.navigate([ item.routerLink ], {
        queryParams: item.queryParams
      });
    }
    if (item['onClick']) {
      item['onClick'](event)
    }
    this.openIconMenu()
  }

  onIconItemHover(menu: Menu, event: Event) {
    if (this.isIconsMenuOpened) {
      return;
    }
    menu.show(event);
  }

  get activeTab(): MenuItem {
    return this.menuItems?.find(item => this.isActiveTab(item))!;
  }

  public isCustomMenuElement(id: string): boolean {
    return Object.values(this.customMenuItems).some(item => item.id === id);
  }


  onCreateMeeting() {
    this.eventService.scheduleEventSidebarState.next(true);
  }

  protected readonly trackByLabel = trackByLabel;
}
