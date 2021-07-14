/*
 * Copyright [2021-2021] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree} from "@angular/router";
import {Observable} from "rxjs";
import {SecurityService} from "./SecurityService";
import {Injectable} from "@angular/core";
import {NzMessageService} from "ng-zorro-antd/message";
import {AuthenticateClient} from "../api/authenticate/AuthenticateClient";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {

  constructor(private securityService: SecurityService
    , private router: Router
    , private messageService: NzMessageService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot)
    : Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    if (this.securityService.authenticated()) {
      return true;
    }
    if (this.securityService.refreshValid()) {
      return this.securityService.refreshToken()
        .pipe(map(succeeded => {
          if (!succeeded) {
            this.securityService.redirectFrom = route.url[0].path;
            this.router.navigateByUrl("login")
          }
          return succeeded;
        }));
    }
    this.messageService.error(`UNAUTHORIZED.`);
    this.securityService.redirectFrom = route.url[0].path;
    return this.router.parseUrl("login");
  }
}