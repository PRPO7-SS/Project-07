import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class DataCacheService {
  private cache: Map<string, any> = new Map();

  constructor() {}

  setCache(key: string, data: any): void {
    this.cache.set(key, data);
  }

  getCache(key: string): any | null {
    return this.cache.get(key) || null;
  }

  clearCache(): void {
    this.cache.clear();
  }
}

