import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DataService } from '../services/data.service';
import { User } from '../models/user';
import { FormsModule } from '@angular/forms';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Router } from '@angular/router';
import { NavigationComponent } from '../navigation/navigation.component';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule, NavigationComponent],
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css'],
  providers: [BsModalService],
})
export class SettingsComponent implements OnInit {
  @ViewChild('success') successTemplate!: TemplateRef<any>;
  @ViewChild('passwordConfirm') confirmPassword!: TemplateRef<any>;

  protected activeTab = 'account'; // Active tab for settings
  protected passwordSettings = {
    oldPassword: '',
    newPassword: '',
    confirmNewPassword: '',
  };
  protected errorMessage = ''; // For displaying errors
  protected successMessage = ''; // For success messages in modal
  protected currentUser: User = {
    fullName: '',
    username: '',
    email: '',
    password: '',
    telephone: '',
  };
  protected originalUser!: User; // For resetting account changes
  protected modalRef?: BsModalRef; // For handling modals

  constructor(
    private readonly dataService: DataService,
    private readonly modalService: BsModalService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.getCurrentUser();
  }

  private getCurrentUser() {
    this.dataService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = { ...user };
        this.originalUser = { ...user };
        this.errorMessage = '';
      },
      error: () => {
        this.errorMessage = 'Error fetching user information.';
      },
    });
  }

  protected updateAccount() {
    this.dataService.updateCurrentUser(this.currentUser).subscribe({
      next: (user: User) => {
        this.errorMessage = '';
        this.currentUser = { ...user };
        this.originalUser = { ...user };

        this.successMessage = 'Account updated successfully.';
        this.openModal(this.successTemplate);

        setTimeout(() => {
          this.closeModal();
        }, 3000);
      },
      error: () => {
        this.errorMessage = 'Error updating account.';
      },
    });
  }

  protected changePassword() {
    if (!this.passwordSettings.newPassword || !this.passwordSettings.confirmNewPassword) {
      this.errorMessage = 'Password fields cannot be empty.';
    } else if (this.passwordSettings.newPassword !== this.passwordSettings.confirmNewPassword) {
      this.errorMessage = 'Passwords do not match.';
    } else {
      this.errorMessage = '';
      this.dataService.updatePassword(this.passwordSettings).subscribe({
        next: () => {
          this.successMessage = 'Password changed successfully.';
          this.openModal(this.successTemplate);

          setTimeout(() => {
            this.closeModal();
            this.resetPasswordFields();
          }, 3000);
        },
        error: () => {
          this.errorMessage = 'Error changing password.';
        },
      });
    }
  }

  protected resetPasswordFields() {
    this.passwordSettings = {
      oldPassword: '',
      newPassword: '',
      confirmNewPassword: '',
    };
  }

  protected logout() {
    this.dataService.logout().subscribe({
      next: () => {
        this.successMessage = 'Logged out successfully.';
        this.openModal(this.successTemplate);

        setTimeout(() => {
          this.closeModal();
          this.router.navigate(['/introductory']);
        }, 3000);
      },
      error: () => {
        this.errorMessage = 'Error during logout.';
      },
    });
  }

  protected openModal(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, {
      class: 'modal-dialog-centered',
      keyboard: false,
      ignoreBackdropClick: true,
    });
  }

  protected closeModal() {
    this.modalRef?.hide();
  }

  protected setActiveTab(tab: string) {
    this.activeTab = tab;
  }

  protected reset() {
    this.currentUser = { ...this.originalUser };
  }
}
