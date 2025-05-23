import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivityTemplateService } from "../../../events/services/activity-template.service";
import { ActivityTypeService } from "../../../events/services/activity-type.service";
import { ActivityScope, ActivityTemplateDto, ActivityTypeDto } from "../../../events/model/activity.model";
import { SearchFilterService } from "../../../../../ui-components/services/search-filter.service";
import {
  CompareOption,
  CustomOperators,
  PageDto,
  SearchOperators
} from "../../../../../ui-components/models/search-filter.model";
import { control, defaultPageOptions } from "../../../../../ui-components/services/utils";
import { FormControl } from "@angular/forms";
import { PaginatorState } from "primeng/paginator";
import { ActivatedRoute, Router } from "@angular/router";
import { OrganizationService } from "../../../organizations/services/organization.service";
import { ConfirmationService, MessageService } from "primeng/api";
import { DialogService } from "primeng/dynamicdialog";
import { filter } from "rxjs";
import { CopyTemplateDialogComponent } from "../../dialogs/copy-template-dialog/copy-template-dialog.component";
import { AuthService } from "../../../../../auth/auth.service";

@Component({
  selector: 'app-activity-library-search',
  templateUrl: './activity-library-search.component.html',
  styleUrl: './activity-library-search.component.css'
})
export class ActivityLibrarySearchComponent implements OnInit, OnDestroy {
  activityTemplates?: PageDto<ActivityTemplateDto>
  selectedType?: ActivityTypeDto;
  activityTypes: ActivityTypeDto[] = [];
  searchFilterControl = new FormControl("");

  isUserTemplates: boolean = false;
  isTeamTemplates: boolean = false;
  teamId?: string;

  constructor(private activityTemplateService: ActivityTemplateService,
              private activityTypeService: ActivityTypeService,
              private filterService: SearchFilterService,
              private organizationService: OrganizationService,
              private messageService: MessageService,
              private router: Router,
              private authService: AuthService,
              private dialogService: DialogService,
              private confirmationService: ConfirmationService,
              private activateRoute: ActivatedRoute) {
  }

  ngOnInit(): void {
    this.filterService.resetFilterService();
    this.loadTypes();
    this.activateRoute.data.subscribe({
      next: (data) => {
        this.isUserTemplates = data['isUserTemplates'];
        this.isTeamTemplates = data['isTeamTemplates'];
        this.loadTemplates();
      }
    });
    if (this.isTeamTemplates) {
      this.activateRoute.params.subscribe(params => {
        this.teamId = params['teamId'];
        this.loadTemplates();
      });
    }
  }

  ngOnDestroy(): void {
    this.filterService.resetFilterService();
  }


  loadTypes() {
    this.activityTypeService.getAllActivityTypes().subscribe({
      next: (types) => {
        this.activityTypes = types;
      }
    })
  }

  loadTemplates() {
    if (this.isUserTemplates) {
      this.loadUserTemplates();
    } else if (this.isTeamTemplates && this.teamId) {
      this.loadTeamTemplates();
    } else {
      this.loadAllTemplates();
    }
  }

  loadAllTemplates() {
    this.filterService.applySearchFilter({
      compareOption: CompareOption.AND,
      property: 'owner.scope',
      operator: CustomOperators.IS_ACTIVITY_SCOPE_EQUAl,
      value: ActivityScope.PUBLIC
    });
    this.activityTemplateService.findActivityTemplates(this.filterService.searchQuery).subscribe({
      next: (templates) => {
        this.activityTemplates = templates;
      }
    })
  }

  loadUserTemplates() {
    this.filterService.applySearchFilter({
      compareOption: CompareOption.AND,
      property: 'owner.createdById',
      operator: SearchOperators.EQUALS,
      value: this.authService.getCurrentUser()?.userId
    });
    this.activityTemplateService.findMyActivityTemplates(this.filterService.searchQuery).subscribe({
      next: (templates) => {
        this.activityTemplates = templates;
      }
    })
  }

  loadTeamTemplates() {
    if (!this.organizationService.getCurrentTeamId()) {
      this.router.navigate(['/app/org/list']);
      return;
    }
    this.filterService.applySearchFilter({
      compareOption: CompareOption.AND,
      property: 'owner.ownerId',
      operator: SearchOperators.EQUALS,
      value: this.teamId
    });
    this.filterService.applySearchFilter({
      compareOption: CompareOption.AND,
      property: 'owner.scope',
      operator: CustomOperators.IS_ACTIVITY_SCOPE_EQUAl,
      value: ActivityScope.PRIVATE
    });
    this.activityTemplateService.findActivityTemplates(this.filterService.searchQuery).subscribe({
      next: (templates) => {
        this.activityTemplates = templates;
      }
    })
  }

  filterByName() {
    if (!this.searchFilterControl.valid) {
      return;
    }
    this.filterService.applySearchFilter({
      compareOption: CompareOption.OR,
      value: this.searchFilterControl.value,
      operator: SearchOperators.LIKE,
      property: "name"
    });
    this.filterService.applySearchFilter({
      compareOption: CompareOption.OR,
      value: this.searchFilterControl.value,
      operator: SearchOperators.LIKE,
      property: "description"
    });
    this.loadTemplates();
  }

  onPageChange(page: PaginatorState) {
    this.filterService.changePagination({
      pageSize: page.rows!,
      pageIndex: page.page!
    });
    this.loadTemplates();
  }

  onSelectActivityType(type?: ActivityTypeDto) {
    this.selectedType = type;
    if (!type) {
      this.filterService.resetFilterService();
    } else {
      this.filterService.applySearchFilter({
        compareOption: CompareOption.AND,
        value: this.selectedType?.type,
        property: 'type.type',
        operator: SearchOperators.EQUALS
      });
    }
    this.loadTemplates();
  }


  copyTemplate(template: ActivityTemplateDto) {
    const ref = this.dialogService.open(CopyTemplateDialogComponent, {
      width: '25vw',
      breakpoints: {
        '1199px': '45vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: 'Copy activity template',
      data: {
        templateId: template.templateId
      }
    });
    ref.onClose
      .pipe(filter(Boolean))
      .subscribe({
        next: (res) => {
          this.editTemplate(res);
        }
      })
  }

  onDeleteTemplate(template: ActivityTemplateDto) {
    this.confirmationService.confirm({
      message: `Are you sure that you want to delete this template ?`,
      header: 'Disconnect service',
      icon: 'pi pi-exclamation-triangle',
      acceptIcon: "none",
      rejectIcon: "none",
      acceptButtonStyleClass: 'p-button-success',
      rejectButtonStyleClass: "p-button-text",
      accept: () => {
        this.deleteTemplate(template);
      }
    });
  }

  deleteTemplate(template: ActivityTemplateDto) {
    this.activityTemplateService.deleteActivityTemplate(template?.templateId!, template.owner?.ownerId!).subscribe({
      next: (template) => {
        this.messageService.add({
          severity: 'success',
          summary: 'Successfully deleted',
          detail: 'Your template successfully deleted',
        });
        this.loadTemplates();
      },
      error: (err) => {
        this.messageService.add({
          severity: 'error',
          summary: 'Failed to delete activity template',
          detail: err.error.message,
        });
      }
    })
  }

  editTemplate(template: ActivityTemplateDto) {
    this.router.navigate([`/app/activity/${ template.templateId! }/edit`])
  }

  onCreateCustomTemplate() {

  }


  protected readonly control = control;
  protected readonly defaultPageOptions = defaultPageOptions;
}
