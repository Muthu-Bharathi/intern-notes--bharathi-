-- Initial schema (matches the tables Hibernate created before Flyway was added).
-- On an existing database this file is skipped (baseline = 1).

CREATE TABLE IF NOT EXISTS books (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    title     VARCHAR(255) NOT NULL,
    author    VARCHAR(255) NOT NULL,
    isbn      VARCHAR(255) NOT NULL,
    available BIT          NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_books_isbn UNIQUE (isbn)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS members (
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    name  VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_members_email UNIQUE (email)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS loans (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    book_id     BIGINT       NOT NULL,
    member_id   BIGINT       NOT NULL,
    loan_date   DATE         NOT NULL,
    return_date DATE,
    status      VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_loans_book   FOREIGN KEY (book_id)   REFERENCES books (id),
    CONSTRAINT fk_loans_member FOREIGN KEY (member_id) REFERENCES members (id)
) ENGINE = InnoDB;
