"use client";

import { useState, useEffect, useRef } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { useAuth } from "@/lib/auth/AuthProvider";

interface Message {
  id: string;
  role: "user" | "agent";
  content: string;
  timestamp: Date;
}

export default function AgentChat() {
  const { user } = useAuth();
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState("");
  const [isTyping, setIsTyping] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const sendMessage = async () => {
    if (!input.trim()) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      role: "user",
      content: input,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput("");
    setIsTyping(true);

    try {
      const response = await fetch("/api/v1/agent/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId: user?.id || "", message: input }),
      });

      if (response.ok) {
        const data = await response.json();
        const agentMessage: Message = {
          id: (Date.now() + 1).toString(),
          role: "agent",
          content: data.response,
          timestamp: new Date(),
        };
        setMessages((prev) => [...prev, agentMessage]);
      }
    } catch (error) {
      console.error("Failed to send message:", error);
    } finally {
      setIsTyping(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const quickActions = [
    "Find remote Java jobs",
    "Increase salary target",
    "Pause today",
    "Skip TCS",
    "Apply only Easy Apply jobs",
    "Search product companies only",
  ];

  return (
    <div className="container mx-auto p-6 h-[calc(100vh-4rem)]">
      <Card className="h-full flex flex-col">
        <CardHeader>
          <CardTitle>Agent Chat</CardTitle>
        </CardHeader>
        <CardContent className="flex-1 flex flex-col overflow-hidden">
          <ScrollArea className="flex-1 mb-4" ref={scrollRef}>
            <div className="space-y-4 p-4">
              {messages.length === 0 && (
                <div className="text-center text-muted-foreground py-8">
                  <p className="mb-4">Welcome to the AI Job Agent</p>
                  <p className="text-sm">Tell me what you need help with</p>
                </div>
              )}
              {messages.map((message) => (
                <div
                  key={message.id}
                  className={`flex ${message.role === "user" ? "justify-end" : "justify-start"}`}
                >
                  <div
                    className={`max-w-[70%] rounded-lg p-3 ${
                      message.role === "user"
                        ? "bg-primary text-primary-foreground"
                        : "bg-muted"
                    }`}
                  >
                    <p className="text-sm">{message.content}</p>
                    <p className="text-xs opacity-70 mt-1">
                      {message.timestamp.toLocaleTimeString()}
                    </p>
                  </div>
                </div>
              ))}
              {isTyping && (
                <div className="flex justify-start">
                  <div className="bg-muted rounded-lg p-3">
                    <p className="text-sm">Agent is thinking...</p>
                  </div>
                </div>
              )}
            </div>
          </ScrollArea>

          <div className="flex flex-wrap gap-2 mb-4">
            {quickActions.map((action, index) => (
              <Button
                key={index}
                variant="outline"
                size="sm"
                onClick={() => setInput(action)}
              >
                {action}
              </Button>
            ))}
          </div>

          <div className="flex gap-2">
            <Input
              placeholder="Type your command..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyPress={handleKeyPress}
            />
            <Button onClick={sendMessage} disabled={isTyping}>
              Send
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
