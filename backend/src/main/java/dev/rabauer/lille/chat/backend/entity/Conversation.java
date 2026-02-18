package dev.rabauer.lille.chat.backend.entity;

import dev.rabauer.lille.chat.backend.dto.ConversationType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations")
public class Conversation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ConversationType type;

  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ConversationParticipant> participants = new ArrayList<>();

  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ChatMessage> messages = new ArrayList<>();

  protected Conversation() {
  }

  public Conversation(String name, ConversationType type) {
    this.name = name;
    this.type = type;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ConversationType getType() {
    return type;
  }

  public void setType(ConversationType type) {
    this.type = type;
  }

  public List<ConversationParticipant> getParticipants() {
    return participants;
  }

  public void setParticipants(List<ConversationParticipant> participants) {
    this.participants = participants;
  }

  public List<ChatMessage> getMessages() {
    return messages;
  }

  public void setMessages(List<ChatMessage> messages) {
    this.messages = messages;
  }

  public void addParticipant(ConversationParticipant participant) {
    participants.add(participant);
    participant.setConversation(this);
  }
}
