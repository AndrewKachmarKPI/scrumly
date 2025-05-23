import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { FormControl, FormGroup, Validators } from "@angular/forms";
import { control, controlArray, specialCharacterValidator } from "../../../../../../ui-components/services/utils";
import { ToggleButtonChangeEvent } from "primeng/togglebutton";
import { ActivityTemplateDto } from "../../../../events/model/activity.model";

@Component({
  selector: 'template-tags',
  templateUrl: './template-tags.component.html',
  styleUrl: './template-tags.component.css'
})
export class TemplateTagsComponent implements OnInit {
  tags: string[] = [];
  templateTagsFormGroup!: FormGroup;

  @Output() onTemplateTagsChange: EventEmitter<string[]> = new EventEmitter<string[]>();

  constructor(private http: HttpClient) {
    this.templateTagsFormGroup = new FormGroup({
      tags: new FormControl([], Validators.compose([
        Validators.required,
        Validators.maxLength(10),
        specialCharacterValidator
      ]))
    });
  }

  ngOnInit(): void {
    this.loadTags();
  }


  onSelectTag(tag: any, event: ToggleButtonChangeEvent) {
    const tagsControl = this.control(this.templateTagsFormGroup, 'tags');
    let tags: string[] = tagsControl.value;
    if (event.checked) {
      tags.push(tag);
    } else {
      const idx = tags.findIndex(value => value === tag);
      tags.splice(idx, 1);
    }
    tagsControl.setValue(tags);
  }

  private loadTags(): void {
    this.http.get<{ tags: string[] }>('/assets/data/tags.json')
      .subscribe(response => this.tags = response.tags);
  }

  get currentValue(): string[] {
    return controlArray(this.templateTagsFormGroup, 'tags').value;
  }

  initValue(dto: ActivityTemplateDto) {
    this.templateTagsFormGroup.setValue({
      tags: dto.tags
    })
  }

  protected readonly control = control;
}
