'use client';

import { useState, useRef, useEffect, useCallback } from 'react';
import { Sparkles, ArrowUp, User, Clock } from 'lucide-react';
import { useAuth } from '@/lib/auth/AuthProvider';
import { apiPost } from '@/lib/api/client';
import { API } from '@/lib/api/endpoints';
import { Avatar, AvatarFallback } from '@/components/ui/avatar';
import { cn } from '@/lib/utils/cn';

interface Message {
  id: string;
  role: 'user' | 'agent';
  content: string;
  timestamp: Date;
}

function Markdown({ text }: { text: string }) {
  const lines = text.split('\n');
  const elements: React.ReactNode[] = [];

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];

    if (line.startsWith('```')) {
      const codeLines: string[] = [];
      i++;
      while (i < lines.length && !lines[i].startsWith('```')) {
        codeLines.push(lines[i]);
        i++;
      }
      elements.push(
        <pre key={`cb-${i}`} className="my-2 overflow-x-auto rounded-lg bg-muted/60 p-3 text-xs leading-relaxed font-mono">
          <code>{codeLines.join('\n')}</code>
        </pre>
      );
      continue;
    }

    if (line.trim() === '') continue;

    if (line.trim().startsWith('- ') || line.trim().startsWith('* ')) {
      const items: React.ReactNode[] = [];
      while (i < lines.length && (lines[i].trim().startsWith('- ') || lines[i].trim().startsWith('* '))) {
        items.push(
          <li key={`li-${i}`} className="text-sm leading-relaxed text-foreground/85">
            {renderInline(lines[i].trim().slice(2))}
          </li>
        );
        i++;
      }
      elements.push(<ul key={`ul-${i}`} className="my-1 list-disc pl-5 space-y-0.5">{items}</ul>);
      i--;
      continue;
    }

    if (/^\d+[.)]\s/.test(line.trim())) {
      elements.push(
        <ol key={`ol-${i}`} className="my-1 list-decimal pl-5">
          <li className="text-sm leading-relaxed text-foreground/85">
            {renderInline(line.trim().replace(/^\d+[.)]\s/, ''))}
          </li>
        </ol>
      );
      continue;
    }

    elements.push(
      <p key={`p-${i}`} className="text-sm leading-relaxed text-foreground/85">
        {renderInline(line)}
      </p>
    );
  }

  return <>{elements}</>;
}

function renderInline(text: string): React.ReactNode {
  const parts: React.ReactNode[] = [];
  let remaining = text;
  let idx = 0;

  while (remaining.length > 0) {
    const boldMatch = remaining.match(/\*\*(.+?)\*\*/);
    const codeMatch = remaining.match(/`([^`]+)`/);

    let firstMatch: RegExpMatchArray | null = null;
    let firstType: 'bold' | 'code' | null = null;
    let firstIndex = Infinity;

    if (boldMatch && boldMatch.index! < firstIndex) {
      firstMatch = boldMatch;
      firstType = 'bold';
      firstIndex = boldMatch.index!;
    }
    if (codeMatch && codeMatch.index! < firstIndex) {
      firstMatch = codeMatch;
      firstType = 'code';
      firstIndex = codeMatch.index!;
    }

    if (!firstMatch) {
      parts.push(<span key={`t-${idx}`}>{remaining}</span>);
      break;
    }

    if (firstIndex > 0) {
      parts.push(<span key={`t-${idx}-pre`}>{remaining.slice(0, firstIndex)}</span>);
    }

    if (firstType === 'bold') {
      parts.push(<strong key={`b-${idx}`} className="font-semibold">{firstMatch[1]}</strong>);
      remaining = remaining.slice(firstIndex + firstMatch[0].length);
    } else {
      parts.push(
        <code key={`c-${idx}`} className="rounded bg-muted/60 px-1.5 py-0.5 text-xs font-mono text-primary">
          {firstMatch[1]}
        </code>
      );
      remaining = remaining.slice(firstIndex + firstMatch[0].length);
    }
    idx++;
  }

  return parts.length > 0 ? parts : text;
}

export default function AgentChat() {
  const { user } = useAuth();
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const bottomRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);
  const formRef = useRef<HTMLFormElement>(null);

  const scrollToBottom = useCallback(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, isTyping, scrollToBottom]);

  const sendMessage = useCallback(async () => {
    const text = input.trim();
    if (!text || isTyping) return;

    const userMsg: Message = {
      id: `user-${Date.now()}`,
      role: 'user',
      content: text,
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMsg]);
    setInput('');
    setIsTyping(true);
    setError(null);

    try {
      if (inputRef.current) {
        inputRef.current.style.height = 'auto';
      }

      const res = await apiPost<{ response: string; type: string }>(API.agent.chat, {
        userId: user?.id ?? '',
        message: text,
      });

      const agentMsg: Message = {
        id: `agent-${Date.now()}`,
        role: 'agent',
        content: res.data.response,
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, agentMsg]);
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setIsTyping(false);
    }
  }, [input, isTyping, user?.id]);

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      formRef.current?.requestSubmit();
    }
  };

  const handleInput = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setInput(e.target.value);
    const el = e.target;
    el.style.height = 'auto';
    el.style.height = `${Math.min(el.scrollHeight, 120)}px`;
  };

  return (
    <div className="flex h-[calc(100dvh-4rem)] flex-col bg-background">
      <header className="flex items-center justify-between border-b border-border/40 px-6 py-3">
        <div className="flex items-center gap-3">
          <Avatar className="h-8 w-8">
            <AvatarFallback className="bg-gradient-primary">
              <Sparkles className="h-4 w-4 text-white" />
            </AvatarFallback>
          </Avatar>
          <div>
            <div className="flex items-center gap-2">
              <span className="text-sm font-semibold">JobPilot AI</span>
              <span className="rounded bg-primary/10 px-1.5 py-0.5 text-[10px] font-medium text-primary">BOT</span>
            </div>
            <div className="flex items-center gap-1.5">
              <span className="relative flex h-1.5 w-1.5">
                <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-success opacity-75" />
                <span className="relative inline-flex h-1.5 w-1.5 rounded-full bg-success" />
              </span>
              <span className="text-[11px] text-muted-foreground">Online</span>
            </div>
          </div>
        </div>
      </header>

      <div className="flex-1 overflow-y-auto scrollbar-premium">
        <div className="mx-auto max-w-3xl px-4 py-6">
          {messages.length === 0 && !isTyping ? (
            <div className="flex flex-col items-center justify-center py-24 text-center">
              <div className="mb-5 flex h-14 w-14 items-center justify-center rounded-2xl bg-gradient-primary shadow-glow">
                <Sparkles className="h-7 w-7 text-white" />
              </div>
              <h2 className="mb-1 text-lg font-semibold">Ask me anything about your job search</h2>
              <p className="max-w-sm text-sm text-muted-foreground">
                I can help find jobs, tailor resumes, write cover letters, and more.
              </p>
            </div>
          ) : (
            <div className="space-y-5">
              {messages.map((msg) => (
                <div
                  key={msg.id}
                  className={cn(
                    'flex animate-fade-in gap-3',
                    msg.role === 'user' ? 'flex-row-reverse' : 'flex-row'
                  )}
                >
                  {msg.role === 'agent' ? (
                    <Avatar className="mt-0.5 h-8 w-8 shrink-0">
                      <AvatarFallback className="bg-gradient-primary">
                        <Sparkles className="h-4 w-4 text-white" />
                      </AvatarFallback>
                    </Avatar>
                  ) : (
                    <Avatar className="mt-0.5 h-8 w-8 shrink-0">
                      <AvatarFallback className="bg-muted text-muted-foreground">
                        <User className="h-4 w-4" />
                      </AvatarFallback>
                    </Avatar>
                  )}

                  <div className={cn('flex min-w-0 flex-col', msg.role === 'user' ? 'items-end' : 'items-start')}>
                    <div className="flex items-center gap-2 mb-0.5">
                      <span className="text-xs font-medium text-foreground/70">
                        {msg.role === 'agent' ? 'JobPilot AI' : 'You'}
                      </span>
                      {msg.role === 'agent' && (
                        <span className="rounded bg-primary/10 px-1 py-0.5 text-[10px] font-medium text-primary leading-none">
                          BOT
                        </span>
                      )}
                      <span className="flex items-center gap-1 text-[11px] text-muted-foreground/60">
                        <Clock className="h-3 w-3" />
                        {msg.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </span>
                    </div>

                    <div
                      className={cn(
                        'rounded-2xl px-4 py-2.5',
                        msg.role === 'user'
                          ? 'bg-primary text-primary-foreground rounded-tr-md'
                          : 'bg-muted/40 text-foreground rounded-tl-md border border-border/30'
                      )}
                    >
                      {msg.role === 'agent' ? (
                        <Markdown text={msg.content} />
                      ) : (
                        <p className="text-sm leading-relaxed">{msg.content}</p>
                      )}
                    </div>
                  </div>
                </div>
              ))}

              {isTyping && (
                <div className="flex animate-fade-in gap-3">
                  <Avatar className="mt-0.5 h-8 w-8 shrink-0">
                    <AvatarFallback className="bg-gradient-primary">
                      <Sparkles className="h-4 w-4 text-white" />
                    </AvatarFallback>
                  </Avatar>
                  <div className="flex min-w-0 flex-col">
                    <div className="flex items-center gap-2 mb-0.5">
                      <span className="text-xs font-medium text-foreground/70">JobPilot AI</span>
                      <span className="rounded bg-primary/10 px-1 py-0.5 text-[10px] font-medium text-primary leading-none">BOT</span>
                    </div>
                    <div className="rounded-2xl rounded-tl-md bg-muted/40 border border-border/30 px-4 py-3">
                      <div className="flex items-center gap-1.5">
                        <span className="typing-dot inline-block h-2 w-2 rounded-full bg-foreground/40" />
                        <span className="typing-dot inline-block h-2 w-2 rounded-full bg-foreground/40" />
                        <span className="typing-dot inline-block h-2 w-2 rounded-full bg-foreground/40" />
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {error && (
                <div className="flex justify-center">
                  <p className="text-xs text-destructive">{error}</p>
                </div>
              )}

              <div ref={bottomRef} />
            </div>
          )}
        </div>
      </div>

      <div className="border-t border-border/40 px-4 py-3">
        <form
          ref={formRef}
          onSubmit={(e) => { e.preventDefault(); sendMessage(); }}
          className="mx-auto flex max-w-3xl items-end gap-2"
        >
          <div className="relative flex-1">
            <textarea
              ref={inputRef}
              value={input}
              onChange={handleInput}
              onKeyDown={handleKeyDown}
              placeholder="Ask me anything..."
              rows={1}
              disabled={isTyping}
              className="w-full resize-none rounded-xl border border-border/50 bg-muted/20 px-4 py-2.5 pr-12 text-sm placeholder:text-muted-foreground/40 focus:border-primary/40 focus:outline-none focus:ring-1 focus:ring-primary/20 disabled:opacity-50"
              style={{ minHeight: '40px', maxHeight: '120px' }}
            />
          </div>
          <button
            type="submit"
            disabled={!input.trim() || isTyping}
            className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-gradient-primary text-white shadow-glow transition-all hover:opacity-90 disabled:opacity-30 disabled:shadow-none"
          >
            <ArrowUp className="h-4 w-4" />
          </button>
        </form>
      </div>
    </div>
  );
}
