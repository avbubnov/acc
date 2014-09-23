package probation;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Book{
 
  private String nameBook;

  public String getNameBook() {
    return nameBook;
  }

  public void setNameBook(String nameBook) {
    this.nameBook = nameBook;
  }
  
} 