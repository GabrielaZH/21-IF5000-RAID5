import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Observable, of } from 'rxjs';
import { Book } from '../models/book.model';
const base_url = environment.base_url;

const httpOptions = {
  headers: new HttpHeaders({
    'Content-Type':  'application/json'
  })
};

@Injectable({
  providedIn: 'root'
})

export class ClientService {


  public book:Book = new Book();

 private extractData(res: Response) {
  let body = res;
  return body || { };
}

constructor( private http:HttpClient ) { }
  

upload(file: File, numNodes:string){
  let formData: FormData = new FormData();
  formData.append("file", file);
  formData.append("numNodes",numNodes)
  const url = `${base_url}/client/uploadFile`;
  return this.http.post<any>(url,formData)
  .pipe(
    map(this.extractData),      
    catchError(this.handleError<any>('upload'))
  );
}

getBooks(): Observable<any>{
  const url = `${base_url}/client/books`;
  return this.http.get(url);
}
getBook(file:string,numNodes:string): Observable<any>{
  let formData: FormData = new FormData();
  formData.append("file", file);
  formData.append("numNodes",numNodes)
  const url = `${base_url}/client/receiveFile`;
  return this.http.post(url,formData);
}


private handleError<T> (operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
  
      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead
  
      // TODO: better job of transforming error for user consumption
      console.log(`${operation} failed: ${error.message}`);
  
      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

}