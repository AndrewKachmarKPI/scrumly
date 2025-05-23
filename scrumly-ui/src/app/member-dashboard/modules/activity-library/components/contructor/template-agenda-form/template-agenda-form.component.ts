import { Component, Input, OnInit } from '@angular/core';
import { DialogService } from 'primeng/dynamicdialog';
import {
  SelectAgendaBlockDialogComponent
} from '../blocks/select-agenda-block-dialog/select-agenda-block-dialog.component';
import {
  ActivityBlockType,
  ActivityBlockTypeDto,
  ActivityTemplateDto,
  CreateActivityBlockConfigRQ, CreateEstimateBlockRQ, CreateItemsBoardRQ,
  CreateQuestionBlockRQ,
  CreateReflectBlockRQ
} from '../../../../events/model/activity.model';
import {
  OrderAgendaBlocksDialogComponent
} from '../blocks/order-agenda-blocks-dialog/order-agenda-blocks-dialog.component';
import { OnBlockUpdateEvent } from '../blocks/create-activity-block/create-activity-block.component';
import { FormGroup } from '@angular/forms';
import { convertBlockDtoToCreateRQ, convertCreateRQToBlockConfig } from '../../../../events/model/event.conversion';

@Component({
  selector: 'template-agenda-form',
  templateUrl: './template-agenda-form.component.html',
  styleUrl: './template-agenda-form.component.css'
})
export class TemplateAgendaFormComponent implements OnInit {
  @Input() isEditMode: boolean = false;
  @Input() ownerId?: string;
  blocks: CreateActivityBlockConfigRQ[] = [];
  formGroups: FormGroup[] = [];
  activeBlock: number = 0;

  constructor(private dialogService: DialogService) {
  }

  ngOnInit(): void {
  }


  onCreateAgendaBlock() {
    const ref = this.dialogService.open(SelectAgendaBlockDialogComponent, {
      width: '80vw',
      breakpoints: {
        '1199px': '75vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: 'Select agenda block type'
    });
    ref.onClose.subscribe({
      next: (selectedBlockType) => {
        if (!selectedBlockType) {
          return;
        }
        const blockType: ActivityBlockTypeDto = selectedBlockType;
        const newBlock = {
          order: this.blocks.length + 1,
          isOptional: false,
          block: {
            name: blockType.name,
            description: blockType.description,
            type: blockType.type,
            isMandatory: true
          }
        };
        this.blocks.push(newBlock);
      }
    })
  }

  onChangeOrderBlocks() {
    const ref = this.dialogService.open(OrderAgendaBlocksDialogComponent, {
      width: '40vw',
      breakpoints: {
        '1199px': '75vw',
        '575px': '90vw'
      },
      resizable: true,
      draggable: false,
      header: 'Order activity agenda blocks',
      data: {
        blocks: this.blocks
      }
    });
  }


  onBlockUpdate(blockConfig: CreateActivityBlockConfigRQ,
                block: OnBlockUpdateEvent<CreateQuestionBlockRQ | CreateReflectBlockRQ | CreateEstimateBlockRQ | CreateItemsBoardRQ>,
                index: number) {
    if (block.blockRQ.type === ActivityBlockType.QUESTION_BLOCK) {
      blockConfig.block = block.blockRQ as CreateQuestionBlockRQ;
    } else if (block.blockRQ.type === ActivityBlockType.REFLECT_BLOCK) {
      blockConfig.block = block.blockRQ as CreateReflectBlockRQ;
    } else if (block.blockRQ.type === ActivityBlockType.ESTIMATE_BLOCK) {
      blockConfig.block = block.blockRQ as CreateEstimateBlockRQ;
    } else if (block.blockRQ.type === ActivityBlockType.ITEM_BOARD_BLOCK) {
      blockConfig.block = block.blockRQ as CreateItemsBoardRQ;
    }
    this.formGroups[index] = block.formGroup;
  }

  onRemoveActivityBlock(block: CreateActivityBlockConfigRQ) {
    const idx = this.blocks.findIndex(value => value.order == block.order);
    this.blocks.splice(idx, 1);
  }

  checkIsBlocksValid() {
    return this.blocks.length != 0 && this.formGroups.every(group => group.valid);
  }

  markAllAsTouched() {
    this.formGroups.forEach(group => group.markAllAsTouched())
  }

  get dtoBlockList() {
    return convertCreateRQToBlockConfig(this.blocks);
  }

  get newBlockList() {
    return this.blocks.filter(value => !value.blockId)
  }

  initValue(dto: ActivityTemplateDto) {
    this.blocks = convertBlockDtoToCreateRQ(dto);
  }
}
