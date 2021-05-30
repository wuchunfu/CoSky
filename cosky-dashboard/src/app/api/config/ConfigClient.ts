import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import {ConfigHistoryDto} from './ConfigHistoryDto';
import {ConfigVersionDto} from './ConfigVersionDto';
import {ConfigDto} from './ConfigDto';

export type ImportPolicy = 'skip' | 'overwrite';

@Injectable({providedIn: 'root'})
export class ConfigClient {

  apiPrefix = environment.governRestApiHost + '/namespaces';

  constructor(private httpClient: HttpClient) {
  }

  getConfigs(namespace: string): Observable<string[]> {
    const apiUrl = this.getConfigsUrl(namespace);
    return this.httpClient.get<string[]>(apiUrl);
  }

  getConfigsUrl(namespace: string): string {
    return `${this.apiPrefix}/${namespace}/configs`;
  }

  getImportUrl(namespace: string): string {
    return this.getConfigsUrl(namespace);
  }

  getConfigApiUrl(namespace: string, configId: string): string {
    return `${this.getConfigsUrl(namespace)}/${configId}`;
  }

  getConfig(namespace: string, configId: string): Observable<ConfigDto> {
    const apiUrl = this.getConfigApiUrl(namespace, configId);
    return this.httpClient.get<ConfigDto>(apiUrl);
  }

  setConfig(namespace: string, configId: string, data: string): Observable<boolean> {
    const apiUrl = this.getConfigApiUrl(namespace, configId);
    return this.httpClient.put<boolean>(apiUrl, data);
  }

  removeConfig(namespace: string, configId: string): Observable<boolean> {
    const apiUrl = this.getConfigApiUrl(namespace, configId);
    return this.httpClient.delete<boolean>(apiUrl);
  }

  rollback(namespace: string, configId: string, targetVersion: number): Observable<boolean> {
    const apiUrl = `${this.getConfigApiUrl(namespace, configId)}/to/${targetVersion}`;
    return this.httpClient.put<boolean>(apiUrl, null);
  }

  getConfigVersions(namespace: string, configId: string): Observable<ConfigVersionDto[]> {
    const apiUrl = `${this.getConfigApiUrl(namespace, configId)}/versions`;
    return this.httpClient.get<ConfigVersionDto[]>(apiUrl);
  }

  getConfigHistory(namespace: string, configId: string, version: number): Observable<ConfigHistoryDto> {
    const apiUrl = `${this.getConfigApiUrl(namespace, configId)}/versions/${version}`;
    return this.httpClient.get<ConfigHistoryDto>(apiUrl);
  }

}