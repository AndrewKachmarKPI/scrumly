import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  ActivityBlockConfigDto,
  ActivityBlockDto,
  ActivityBlockType,
  ActivityTemplateDto
} from '../../../events/model/activity.model';
import { MenuItem } from 'primeng/api';
import { AuthService } from '../../../../../auth/auth.service';
import { Router } from '@angular/router';
import { OverlayPanel } from 'primeng/overlaypanel';
import { Menu } from 'primeng/menu';

@Component({
  selector: 'prime-template-card',
  templateUrl: './template-card.component.html',
  styleUrl: './template-card.component.css'
})
export class TemplateCardComponent {
  @Input() template?: ActivityTemplateDto;
  @Output() onCopyTemplate: EventEmitter<ActivityTemplateDto> = new EventEmitter();
  @Output() onEditTemplate: EventEmitter<ActivityTemplateDto> = new EventEmitter();
  @Output() onDeleteTemplate: EventEmitter<ActivityTemplateDto> = new EventEmitter();

  constructor(private authService: AuthService,
              private router: Router) {
  }

  getMenuItems(template: ActivityTemplateDto): MenuItem[] {
    const items = [
      {
        label: 'Copy template',
        icon: 'pi pi-copy',
        command: () => this.onCopyTemplate.emit(template)
      }
    ];
    if (this.authService.isCurrentUser(template.owner?.createdById!)) {
      items.push({
        label: 'Edit template',
        icon: 'pi pi-file-edit',
        command: () => this.onEditTemplate.emit(template)
      })
      items.push({
        label: 'Delete template',
        icon: 'pi pi-trash',
        command: () => this.onDeleteTemplate.emit(template)
      })
    }
    return items;
  }


  getBlockDescription(template: ActivityTemplateDto): ActivityBlockConfigDto {
    if (template?.type?.type === 'Standup') {
      return template.blocks!
        .filter(value => value.blockType === ActivityBlockType.QUESTION_BLOCK ||
          value.blockType === ActivityBlockType.ITEM_BOARD_BLOCK)
        .reduce((minBlock, currentBlock) =>
          currentBlock.blockOrder < minBlock.blockOrder ? currentBlock : minBlock
        );
    } else if (template.type?.type === 'Retrospective') {
      return template.blocks!
        .filter(value => value.blockType === ActivityBlockType.REFLECT_BLOCK)
        .reduce((minBlock, currentBlock) =>
          currentBlock.blockOrder < minBlock.blockOrder ? currentBlock : minBlock
        );
    }
    return template.blocks?.reduce((minBlock, currentBlock) =>
      currentBlock.blockOrder < minBlock.blockOrder ? currentBlock : minBlock
    )!;
  }

  openDetails(templateId: string, menu: Menu) {
    if (menu.visible) {
      return;
    }
    this.router.navigate([ '/app/activity/' + templateId + '/view' ])
  }

}
