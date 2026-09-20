package model;

import java.util.Objects;

public class Book {


    private Integer id;
    private String title;
    private Integer totalCopies;

    public Book(Integer id, String title, Integer totalCopies) {
        this.id = id;
        this.title = title;
        this.totalCopies = totalCopies;
    }
    public Book( String title, Integer totalCopies) {
        this.title = title;
        this.totalCopies = totalCopies;
    }

    public Integer getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    @Override
    public String toString() {
        return id + " | " + title + " | " + totalCopies;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Book book)) return false;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
