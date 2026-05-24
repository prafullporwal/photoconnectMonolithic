import { useEffect, useState } from 'react';
import { Link, NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../lib/auth';

export function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  // Close mobile menu on route change
  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    `relative px-3 py-2 text-sm font-medium transition ${
      isActive
        ? 'text-indigo-700'
        : 'text-slate-600 hover:text-slate-900'
    }`;

  const mobileNavLinkClass = ({ isActive }: { isActive: boolean }) =>
    `block px-4 py-3.5 text-base font-medium transition ${
      isActive
        ? 'text-indigo-700 bg-indigo-50/80'
        : 'text-slate-700 hover:bg-slate-50'
    }`;

  const handleLogout = async () => {
    await logout();
    navigate('/', { replace: true });
  };

  return (
    <header className="sticky top-0 z-30 border-b border-slate-200/70 bg-white/75 backdrop-blur-lg supports-[backdrop-filter]:bg-white/60">
      <div className="mx-auto flex h-14 max-w-6xl items-center justify-between gap-4 px-4">
        {/* Logo */}
        <Link to="/" className="flex shrink-0 items-center gap-2">
          <span
            aria-hidden="true"
            className="grid h-8 w-8 place-items-center rounded-lg bg-gradient-to-br from-indigo-500 via-violet-500 to-fuchsia-500 text-white shadow-sm"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2.2"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="h-4 w-4"
            >
              <path d="M14.5 4h-5L8 6H4a1 1 0 0 0-1 1v11a1 1 0 0 0 1 1h16a1 1 0 0 0 1-1V7a1 1 0 0 0-1-1h-4l-1.5-2Z" />
              <circle cx="12" cy="13" r="3.5" />
            </svg>
          </span>
          <span className="text-base font-bold tracking-tight text-slate-900">
            PhotoConnect
          </span>
        </Link>

        {/* Desktop nav — hidden on mobile */}
        <nav className="hidden flex-1 items-center gap-0.5 md:flex">
          {user?.role !== 'PHOTOGRAPHER' && (
            <NavLink to="/" end className={navLinkClass}>
              {({ isActive }) => (
                <>
                  Browse
                  {isActive && <ActiveUnderline />}
                </>
              )}
            </NavLink>
          )}

          {user?.role === 'PHOTOGRAPHER' && (
            <>
              <NavLink to="/me/profile" className={navLinkClass}>
                {({ isActive }) => (<>Profile{isActive && <ActiveUnderline />}</>)}
              </NavLink>
              <NavLink to="/me/portfolio" className={navLinkClass}>
                {({ isActive }) => (<>Portfolio{isActive && <ActiveUnderline />}</>)}
              </NavLink>
              <NavLink to="/me/availability" className={navLinkClass}>
                {({ isActive }) => (<>Calendar{isActive && <ActiveUnderline />}</>)}
              </NavLink>
              <NavLink to="/me/inbox" className={navLinkClass}>
                {({ isActive }) => (<>Inbox{isActive && <ActiveUnderline />}</>)}
              </NavLink>
            </>
          )}

          {user?.role === 'CUSTOMER' && (
            <>
              <NavLink to="/me/profile" className={navLinkClass}>
                {({ isActive }) => (<>Profile{isActive && <ActiveUnderline />}</>)}
              </NavLink>
              <NavLink to="/me/favorites" className={navLinkClass}>
                {({ isActive }) => (<>Saved{isActive && <ActiveUnderline />}</>)}
              </NavLink>
              <NavLink to="/me/inquiries" className={navLinkClass}>
                {({ isActive }) => (<>Inquiries{isActive && <ActiveUnderline />}</>)}
              </NavLink>
            </>
          )}

          {user?.role === 'ADMIN' && (
            <NavLink to="/admin" className={navLinkClass}>
              {({ isActive }) => (<>Admin{isActive && <ActiveUnderline />}</>)}
            </NavLink>
          )}
        </nav>

        {/* Right-side actions */}
        <div className="flex items-center gap-2">
          {user ? (
            <>
              <span className="hidden items-center gap-1.5 rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-600 sm:inline-flex">
                <span className="inline-block h-1.5 w-1.5 rounded-full bg-emerald-500" />
                {user.email}
              </span>
              <button
                onClick={handleLogout}
                className="hidden rounded-md px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-100 hover:text-slate-900 md:block"
              >
                Log out
              </button>
            </>
          ) : (
            <>
              <Link
                to="/login"
                className="hidden rounded-md px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-100 hover:text-slate-900 md:block"
              >
                Log in
              </Link>
              <Link
                to="/register"
                className="rounded-md bg-gradient-to-br from-indigo-600 to-violet-600 px-3.5 py-1.5 text-sm font-semibold text-white shadow-sm transition hover:shadow-md hover:brightness-110"
              >
                Sign up
              </Link>
            </>
          )}

          {/* Hamburger — mobile only */}
          <button
            type="button"
            aria-label={menuOpen ? 'Close menu' : 'Open menu'}
            aria-expanded={menuOpen}
            onClick={() => setMenuOpen(v => !v)}
            className="rounded-md p-2 text-slate-600 hover:bg-slate-100 hover:text-slate-900 md:hidden"
          >
            {menuOpen ? (
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-5 w-5" aria-hidden="true">
                <path d="M18 6 6 18M6 6l12 12" />
              </svg>
            ) : (
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="h-5 w-5" aria-hidden="true">
                <line x1="4" x2="20" y1="6" y2="6" />
                <line x1="4" x2="20" y1="12" y2="12" />
                <line x1="4" x2="20" y1="18" y2="18" />
              </svg>
            )}
          </button>
        </div>
      </div>

      {/* Mobile dropdown menu */}
      {menuOpen && (
        <div className="border-t border-slate-200 bg-white/95 backdrop-blur-lg md:hidden">
          <nav className="divide-y divide-slate-100">
            {user?.role !== 'PHOTOGRAPHER' && (
              <NavLink to="/" end className={mobileNavLinkClass}>
                Browse
              </NavLink>
            )}
            {user?.role === 'PHOTOGRAPHER' && (
              <>
                <NavLink to="/me/profile" className={mobileNavLinkClass}>Profile</NavLink>
                <NavLink to="/me/portfolio" className={mobileNavLinkClass}>Portfolio</NavLink>
                <NavLink to="/me/availability" className={mobileNavLinkClass}>Calendar</NavLink>
                <NavLink to="/me/inbox" className={mobileNavLinkClass}>Inbox</NavLink>
              </>
            )}
            {user?.role === 'CUSTOMER' && (
              <>
                <NavLink to="/me/profile" className={mobileNavLinkClass}>Profile</NavLink>
                <NavLink to="/me/favorites" className={mobileNavLinkClass}>Saved</NavLink>
                <NavLink to="/me/inquiries" className={mobileNavLinkClass}>Inquiries</NavLink>
              </>
            )}
            {user?.role === 'ADMIN' && (
              <NavLink to="/admin" className={mobileNavLinkClass}>Admin</NavLink>
            )}
          </nav>

          {/* Auth section inside mobile menu */}
          <div className="border-t border-slate-200 px-4 py-3">
            {user ? (
              <div className="flex items-center justify-between gap-3">
                <span className="truncate text-xs text-slate-500">{user.email}</span>
                <button
                  onClick={handleLogout}
                  className="shrink-0 rounded-md border border-slate-200 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                >
                  Log out
                </button>
              </div>
            ) : (
              <div className="flex gap-2">
                <Link
                  to="/login"
                  className="flex-1 rounded-md border border-slate-200 px-4 py-2 text-center text-sm font-medium text-slate-700 hover:bg-slate-50"
                >
                  Log in
                </Link>
                <Link
                  to="/register"
                  className="flex-1 rounded-md bg-gradient-to-br from-indigo-600 to-violet-600 px-4 py-2 text-center text-sm font-semibold text-white shadow-sm"
                >
                  Sign up
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}

function ActiveUnderline() {
  return (
    <span
      aria-hidden="true"
      className="absolute inset-x-3 -bottom-px h-0.5 rounded-full bg-gradient-to-r from-indigo-500 to-violet-500"
    />
  );
}
