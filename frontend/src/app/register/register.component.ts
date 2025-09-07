import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { AuthService, RegisterRequest, Role } from '../services/auth/auth.service';

type Category = { id: number; name: string };

// Custom validator for password confirmation
function passwordMatchValidator(form: FormGroup) {
  const password = form.get('password');
  const confirmPassword = form.get('confirmPassword');
  return password && confirmPassword && password.value === confirmPassword.value
    ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  private auth = inject(AuthService);
  private router = inject(Router);
  private http = inject(HttpClient);
  private fb = inject(FormBuilder);

  categories: Category[] = [];
  categoriesLoading = false;
  loading = false;
  error = '';
  success = '';

  registerForm: FormGroup = this.fb.group({
    firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(120)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]],
    role: ['CLIENT', [Validators.required]],
    categoryId: [null],
    metier: [''],
    localisation: [''],
    description: ['']
  }, { validators: passwordMatchValidator });

  ngOnInit() {
    this.loadCategories();
    this.setupRoleValidation();
  }

  private loadCategories() {
    this.categoriesLoading = true;
    const params = new HttpParams().set('page', 0).set('size', 100);
    this.http.get<{content: Category[]}>('http://localhost:8091/api/categories', { params })
      .subscribe({
        next: (p) => this.categories = p?.content ?? [],
        complete: () => this.categoriesLoading = false,
        error: () => this.categoriesLoading = false
      });
  }

  private setupRoleValidation() {
    this.registerForm.get('role')?.valueChanges.subscribe(role => {
      const categoryControl = this.registerForm.get('categoryId');
      const metierControl = this.registerForm.get('metier');
      
      if (role === 'ARTISAN') {
        categoryControl?.setValidators([Validators.required]);
        metierControl?.setValidators([Validators.required, Validators.maxLength(120)]);
      } else {
        categoryControl?.clearValidators();
        metierControl?.clearValidators();
        categoryControl?.setValue(null);
        metierControl?.setValue('');
        this.registerForm.get('localisation')?.setValue('');
        this.registerForm.get('description')?.setValue('');
      }
      categoryControl?.updateValueAndValidity();
      metierControl?.updateValueAndValidity();
    });
  }

  trackCat = (_: number, c: Category) => c.id;

  setRole(role: Role) {
    this.registerForm.patchValue({ role });
  }

  get isArtisan() {
    return this.registerForm.get('role')?.value === 'ARTISAN';
  }

  getFieldError(fieldName: string): string | null {
    const field = this.registerForm.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) return `${this.getFieldLabel(fieldName)} is required`;
      if (field.errors['email']) return 'Please enter a valid email address';
      if (field.errors['minlength']) return `${this.getFieldLabel(fieldName)} must be at least ${field.errors['minlength'].requiredLength} characters`;
      if (field.errors['maxlength']) return `${this.getFieldLabel(fieldName)} must be no more than ${field.errors['maxlength'].requiredLength} characters`;
    }
    if (this.registerForm.errors?.['passwordMismatch'] && (fieldName === 'confirmPassword') && field?.touched) {
      return 'Passwords do not match';
    }
    return null;
  }

  private getFieldLabel(fieldName: string): string {
    const labels: { [key: string]: string } = {
      firstName: 'First name',
      lastName: 'Last name',
      email: 'Email',
      password: 'Password',
      confirmPassword: 'Confirm password',
      categoryId: 'Category',
      metier: 'Craft/Trade'
    };
    return labels[fieldName] || fieldName;
  }

  onSubmit() {
    this.error = '';
    this.success = '';

    // Mark all fields as touched to show validation errors
    this.registerForm.markAllAsTouched();

    if (this.registerForm.invalid) {
      this.error = 'Please fix the validation errors above.';
      return;
    }

    const formValue = this.registerForm.value;
    const payload: RegisterRequest = {
      // Map frontend field names to backend expected names
      nom: formValue.firstName.trim(),      // Backend expects 'nom'
      prenom: formValue.lastName.trim(),    // Backend expects 'prenom' 
      email: formValue.email.trim(),
      password: formValue.password,
      role: formValue.role,
      categoryId: formValue.role === 'ARTISAN' ? formValue.categoryId : null,
      metier: formValue.role === 'ARTISAN' ? (formValue.metier || null) : null,
      localisation: formValue.role === 'ARTISAN' ? (formValue.localisation || null) : null,
      description: formValue.role === 'ARTISAN' ? (formValue.description || null) : null
    };

    this.loading = true;
    this.auth.register(payload).subscribe({
      next: () => {
        this.success = 'Registration successful! Redirecting to login...';
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (e) => {
        console.error('Registration error:', e);
        this.error = e?.error?.message || 'Registration failed. Please try again.';
      },
      complete: () => this.loading = false
    });
  }
}
