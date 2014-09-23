package probation;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Student{
  private String gender;

  private String name;

  private int age;
  
  private Book book;

   public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender= gender;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAge() {
    return age;
  }
  public void setAge(int age) {
    this.age= age;
  }
  
  public Book getBook() {
	    return book;
  }
  public void setBook(Book book) {
	    this.book= book;
  }
} 