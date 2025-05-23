export interface GetIssuePickerSuggestions {
  sections: Section[];
}

export interface Section {
  id: string;
  issues: Issue[];
  label: string;
  msg: string;
  sub: string;
}

export interface Issue {
  id: number;
  img: string;
  key: string;
  keyHtml: string;
  summary: string;
  summaryText: string;
}
