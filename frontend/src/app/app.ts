import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

interface Project {
  id: string;
  name: string;
}
interface EntryType {
  id: string;
  name: string;
}
interface Ticket {
  id: string;
  project_id: string;
  entry_type_id: string;
  title: string;
  description: string;
}
interface SportRecord {
  id: string;
  date: string;
  duration_minutes: number;
  description: string;
  fields: { sport: string };
}
interface RecordList {
  records: SportRecord[];
  totalMinutes: number;
}

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly http = inject(HttpClient);
  readonly projects = signal<Project[]>([]);
  readonly types = signal<EntryType[]>([]);
  readonly tickets = signal<Ticket[]>([]);
  readonly selectedId = signal('');
  readonly selected = computed(() => this.tickets().find((t) => t.id === this.selectedId()));
  readonly records = signal<SportRecord[]>([]);
  readonly totalMinutes = signal(0);
  readonly loading = signal(true);
  readonly loadingRecords = signal(false);
  readonly saving = signal(false);
  readonly creating = signal(false);
  readonly error = signal('');
  readonly notice = signal('');
  projectId = '';
  ticketTitle = '';
  ticketDescription = '';
  date = this.today();
  sport = '';
  duration: number | null = null;
  description = '';
  private loadSequence = 0;

  constructor() {
    void this.load();
  }

  async load() {
    this.loading.set(true);
    this.error.set('');
    try {
      const [projects, types, tickets] = await Promise.all([
        firstValueFrom(this.http.get<Project[]>('/api/projects')),
        firstValueFrom(this.http.get<EntryType[]>('/api/entry-types')),
        firstValueFrom(this.http.get<Ticket[]>('/api/tickets')),
      ]);
      this.projects.set(projects);
      this.types.set(types);
      this.tickets.set(tickets);
      this.projectId = projects[0]?.id ?? '';
      if (tickets.length) await this.selectTicket(tickets[0].id);
    } catch (error) {
      this.showError(error);
    } finally {
      this.loading.set(false);
    }
  }

  async selectTicket(id: string) {
    const sequence = ++this.loadSequence;
    this.selectedId.set(id);
    this.records.set([]);
    this.totalMinutes.set(0);
    this.loadingRecords.set(true);
    this.error.set('');
    this.notice.set('');
    try {
      const result = await firstValueFrom(this.http.get<RecordList>(`/api/tickets/${id}/records`));
      if (sequence === this.loadSequence) {
        this.records.set(result.records);
        this.totalMinutes.set(result.totalMinutes);
      }
    } catch (error) {
      if (sequence === this.loadSequence) this.showError(error);
    } finally {
      if (sequence === this.loadSequence) this.loadingRecords.set(false);
    }
  }

  async createTicket(form: NgForm) {
    if (form.invalid || !this.ticketTitle.trim() || this.creating()) return;
    const type = this.types().find((t) => t.id === '00000000-0000-0000-0000-000000000002');
    if (!type) {
      this.error.set('Der vorbereitete Sport-Typ fehlt. Bitte Backend prüfen.');
      return;
    }
    this.creating.set(true);
    this.error.set('');
    this.notice.set('');
    try {
      const ticket = await firstValueFrom(
        this.http.post<Ticket>('/api/tickets', {
          projectId: this.projectId,
          entryTypeId: type.id,
          title: this.ticketTitle.trim(),
          description: this.ticketDescription,
        }),
      );
      this.tickets.update((tickets) => [...tickets, ticket]);
      form.resetForm({ project: this.projectId, title: '', ticketDescription: '' });
      await this.selectTicket(ticket.id);
      this.notice.set('Ticket angelegt. Du kannst jetzt Zeit erfassen.');
    } catch (error) {
      this.showError(error);
    } finally {
      this.creating.set(false);
    }
  }

  async saveRecord(form: NgForm) {
    if (
      form.invalid ||
      !this.sport.trim() ||
      !Number.isInteger(this.duration) ||
      !this.selected() ||
      this.saving()
    )
      return;
    const id = this.selectedId();
    this.saving.set(true);
    this.error.set('');
    this.notice.set('');
    try {
      await firstValueFrom(
        this.http.post(`/api/tickets/${id}/records`, {
          date: this.date,
          sport: this.sport.trim(),
          durationMinutes: this.duration,
          description: this.description,
        }),
      );
      form.resetForm({ date: this.date, sport: this.sport, duration: null, description: '' });
      await this.selectTicket(id);
      if (!this.error()) this.notice.set('Sporterfassung gespeichert.');
    } catch (error) {
      this.showError(error);
    } finally {
      this.saving.set(false);
    }
  }

  projectName(id: string) {
    return this.projects().find((p) => p.id === id)?.name ?? 'Projekt';
  }
  formatDuration(minutes: number) {
    const hours = Math.floor(minutes / 60),
      rest = minutes % 60;
    return hours ? `${hours} h${rest ? ` ${rest} min` : ''}` : `${rest} min`;
  }
  formatDate(value: string) {
    return value.split('-').reverse().join('.');
  }
  private today() {
    return new Intl.DateTimeFormat('sv-SE', { timeZone: 'Europe/Vienna' }).format(new Date());
  }
  private showError(error: unknown) {
    this.error.set(
      error instanceof HttpErrorResponse
        ? error.status === 0 || error.status >= 500
          ? 'Der Server ist nicht erreichbar oder konnte die Anfrage nicht verarbeiten. Bitte Backend und Datenbank prüfen und erneut versuchen.'
          : (error.error?.message ?? 'Die Eingaben konnten nicht gespeichert werden.')
        : 'Die Anfrage ist fehlgeschlagen. Bitte erneut versuchen.',
    );
  }
}
