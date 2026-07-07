'use client';

import { useState, useEffect, useRef } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { ScrollArea } from '@/components/ui/scroll-area';
import { useAuth } from '@/lib/auth/AuthProvider';
import { agentPost, API } from '@/lib/api/agent-client';
import {
  Send,
  Bot,
  User,
  Sparkles,
  Zap,
  ArrowUp,
  Mic,
  Briefcase,
  FileText,
  Search,
  TrendingUp,
} from 'lucide-react';

interface Message {
  id: string;
  role: 'user' | 'agent';
  content: string;
  timestamp: Date;
}

export default function AgentChat() {
  const { user } = useAuth();
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [messages]);

  const sendMessage = async () => {
    if (!input.trim()) return;

    const userMessage: Message = {
      id: Date.now().toString(),
      role: 'user',
      content: input,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsTyping(true);

    try {
      const data = await agentPost<{ response: string }>(API.agent.chat, {
        userId: user?.id || '',
        message: input,
      });
      const agentMessage: Message = {
        id: (Date.now() + 1).toString(),
        role: 'agent',
        content: data.response,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, agentMessage]);
    } catch (error) {
      console.error('Failed to send message:', error);
    } finally {
      setIsTyping(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  const quickActions = [
    { label: 'Find remote Java jobs', icon: Search, color: 'text-primary' },
    { label: 'Score my resume', icon: TrendingUp, color: 'text-success' },
    { label: 'Generate cover letter', icon: FileText, color: 'text-warning' },
    { label: 'Practice interview', icon: Mic, color: 'text-info' },
    { label: 'Research company', icon: Briefcase, color: 'text-primary' },
    { label: 'Pause agent', icon: Zap, color: 'text-destructive' },
  ];

  return (
    <div className="flex h-[calc(100vh-4rem)] flex-col">
      {/* Header */}
      <div className="flex items-center justify-between border-b border-border/50 px-6 py-4">
        <div className="flex items-center gap-3">
          <div className="relative">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-primary shadow-glow">
              <Bot className="h-5 w-5 text-white" />
            </div>
            <span className="absolute -bottom-0.5 -right-0.5 h-3 w-3 rounded-full bg-success border-2 border-background" />
          </div>
          <div>
            <h1 className="text-lg font-bold">Agent Chat</h1>
            <p className="text-xs text-muted-foreground">AI Job Agent • Always online</p>
          </div>
        </div>
        <div className="flex items-center gap-2 text-xs text-muted-foreground">
          <Sparkles className="h-3.5 w-3.5 text-primary animate-pulse" />
          <span>Powered by Ollama</span>
        </div>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-hidden">
        <ScrollArea className="h-full" ref={scrollRef}>
          <div className="space-y-4 p-6">
            {messages.length === 0 && (
              <div className="flex flex-col items-center justify-center py-16 text-center">
                <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-2xl bg-gradient-primary shadow-glow-lg animate-float">
                  <Bot className="h-10 w-10 text-white" />
                </div>
                <h2 className="text-2xl font-bold mb-2">What can I help with?</h2>
                <p className="text-muted-foreground max-w-md">
                  I can find jobs, tailor your resume, write cover letters, prepare you for interviews,
                  and automate applications.
                </p>
              </div>
            )}

            {messages.map((message, i) => (
              <div
                key={message.id}
                className={`flex animate-fade-in ${
                  message.role === 'user' ? 'justify-end' : 'justify-start'
                }`}
              >
                {message.role === 'agent' && (
                  <div className="mr-3 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-primary/10 mt-1">
                    <Bot className="h-4 w-4 text-primary" />
                  </div>
                )}
                <div
                  className={`max-w-[75%] rounded-2xl px-4 py-3 ${
                    message.role === 'user'
                      ? 'bg-gradient-primary text-white'
                      : 'glass border border-border/50'
                  }`}
                >
                  <p className="text-sm leading-relaxed">{message.content}</p>
                  <p className={`text-[10px] mt-1.5 ${
                    message.role === 'user' ? 'text-white/60' : 'text-muted-foreground'
                  }`}>
                    {message.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </p>
                </div>
                {message.role === 'user' && (
                  <div className="ml-3 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-muted mt-1">
                    <User className="h-4 w-4 text-muted-foreground" />
                  </div>
                )}
              </div>
            ))}

            {isTyping && (
              <div className="flex animate-fade-in justify-start">
                <div className="mr-3 flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-primary/10 mt-1">
                  <Bot className="h-4 w-4 text-primary" />
                </div>
                <div className="glass rounded-2xl border border-border/50 px-4 py-3">
                  <div className="flex items-center gap-1.5">
                    <span className="typing-dot h-2 w-2 rounded-full bg-primary" />
                    <span className="typing-dot h-2 w-2 rounded-full bg-primary" />
                    <span className="typing-dot h-2 w-2 rounded-full bg-primary" />
                  </div>
                </div>
              </div>
            )}
          </div>
        </ScrollArea>
      </div>

      {/* Quick Actions */}
      {messages.length === 0 && (
        <div className="border-t border-border/50 px-6 py-3">
          <div className="flex flex-wrap gap-2">
            {quickActions.map((action) => {
              const Icon = action.icon;
              return (
                <Button
                  key={action.label}
                  variant="outline"
                  size="sm"
                  className="gap-2 glass border-border/50 hover:border-primary/30 hover-lift"
                  onClick={() => setInput(action.label)}
                >
                  <Icon className={`h-3.5 w-3.5 ${action.color}`} />
                  {action.label}
                </Button>
              );
            })}
          </div>
        </div>
      )}

      {/* Input */}
      <div className="border-t border-border/50 px-6 py-4">
        <div className="flex items-center gap-3">
          <div className="relative flex-1">
            <Input
              ref={inputRef}
              placeholder="Tell the agent what to do..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={handleKeyPress}
              className="h-12 rounded-xl border-border/50 bg-muted/30 pl-4 pr-12 text-sm focus:border-primary/50 focus:ring-primary/20"
            />
          </div>
          <Button
            onClick={sendMessage}
            disabled={isTyping || !input.trim()}
            size="icon"
            className="h-12 w-12 rounded-xl bg-gradient-primary hover:opacity-90 shadow-glow transition-all disabled:opacity-50"
          >
            <ArrowUp className="h-5 w-5" />
          </Button>
        </div>
      </div>
    </div>
  );
}
