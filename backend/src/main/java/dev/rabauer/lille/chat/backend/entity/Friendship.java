package dev.rabauer.lille.chat.backend.entity;

import dev.rabauer.lille.chat.backend.dto.FriendshipStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friendships")
public class Friendship {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "requester_id", nullable = false)
  private User requester;

  @ManyToOne
  @JoinColumn(name = "addressee_id", nullable = false)
  private User addressee;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private FriendshipStatus status;

  @Column(nullable = false)
  private Instant createdAt;

  protected Friendship() {
  }

  public Friendship(User requester, User addressee) {
    this.requester = requester;
    this.addressee = addressee;
    this.status = FriendshipStatus.PENDING;
    this.createdAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public User getRequester() {
    return requester;
  }

  public void setRequester(User requester) {
    this.requester = requester;
  }

  public User getAddressee() {
    return addressee;
  }

  public void setAddressee(User addressee) {
    this.addressee = addressee;
  }

  public FriendshipStatus getStatus() {
    return status;
  }

  public void setStatus(FriendshipStatus status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
