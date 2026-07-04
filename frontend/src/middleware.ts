import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

const PUBLIC_PATHS = ['/login', '/register', '/forgot-password', '/auth/callback', '/'];

export function middleware(request: NextRequest) {
  const token = request.cookies.get('accessToken')?.value;
  const { pathname } = request.nextUrl;

  const isPublic = PUBLIC_PATHS.some((path) => {
    if (path === '/') return pathname === '/';
    return pathname.startsWith(path);
  });

  if (!token && !isPublic) {
    return NextResponse.redirect(new URL('/login', request.url));
  }

  if (token && (pathname === '/login' || pathname === '/register')) {
    return NextResponse.redirect(new URL('/dashboard', request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/((?!api|_next/static|_next/image|favicon.ico).*)'],
};
