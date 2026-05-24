import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api, errorMessage } from '../lib/api';
import type { AdminStats, AdminUser, AdminContentItem, PhotographerProfile, Inquiry } from '../lib/types';

// ── API calls ─────────────────────────────────────────────────────────────────

const fetchStats         = () => api.get<AdminStats>('/admin/stats').then(r => r.data);
const fetchUsers         = () => api.get<AdminUser[]>('/admin/users').then(r => r.data);
const fetchPhotographers = () => api.get<PhotographerProfile[]>('/admin/photographers').then(r => r.data);
const fetchInquiries     = () => api.get<Inquiry[]>('/admin/inquiries').then(r => r.data);
const fetchContent       = () => api.get<AdminContentItem[]>('/admin/content').then(r => r.data);

// Poll intervals — stats refresh fastest, content (heavy) slowest
const POLL_STATS       = 10_000;   // 10 s
const POLL_USERS       = 15_000;   // 15 s
const POLL_PHOTOS      = 30_000;   // 30 s
const POLL_INQUIRIES   = 15_000;   // 15 s
const POLL_CONTENT     = 30_000;   // 30 s

type Tab = 'overview' | 'users' | 'photographers' | 'inquiries' | 'content';

export function AdminDashboardPage() {
  const [tab, setTab] = useState<Tab>('overview');
  const qc = useQueryClient();

  const statsQ = useQuery({
    queryKey: ['admin', 'stats'],
    queryFn: fetchStats,
    refetchInterval: POLL_STATS,
  });
  const usersQ = useQuery({
    queryKey: ['admin', 'users'],
    queryFn: fetchUsers,
    enabled: tab === 'users',
    refetchInterval: tab === 'users' ? POLL_USERS : false,
  });
  const photosQ = useQuery({
    queryKey: ['admin', 'photographers'],
    queryFn: fetchPhotographers,
    enabled: tab === 'photographers',
    refetchInterval: tab === 'photographers' ? POLL_PHOTOS : false,
  });
  const inquiriesQ = useQuery({
    queryKey: ['admin', 'inquiries'],
    queryFn: fetchInquiries,
    enabled: tab === 'inquiries',
    refetchInterval: tab === 'inquiries' ? POLL_INQUIRIES : false,
  });
  const contentQ = useQuery({
    queryKey: ['admin', 'content'],
    queryFn: fetchContent,
    enabled: tab === 'content',
    refetchInterval: tab === 'content' ? POLL_CONTENT : false,
  });

  const toggleUser = useMutation({
    mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) =>
      api.patch<AdminUser>(`/admin/users/${id}/${enabled ? 'enable' : 'disable'}`).then(r => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'users'] }),
  });

  const deleteContent = useMutation({
    mutationFn: (id: string) => api.delete(`/admin/content/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['admin', 'content'] }),
  });

  // Active query for the current tab — drives the toolbar
  const activeQ = {
    overview:      statsQ,
    users:         usersQ,
    photographers: photosQ,
    inquiries:     inquiriesQ,
    content:       contentQ,
  }[tab];

  const refetchActive = () => activeQ.refetch();

  const tabs: { id: Tab; label: string }[] = [
    { id: 'overview',      label: 'Overview' },
    { id: 'users',         label: 'Users' },
    { id: 'photographers', label: 'Photographers' },
    { id: 'inquiries',     label: 'Inquiries' },
    { id: 'content',       label: 'Content' },
  ];

  return (
    <div className="mx-auto max-w-6xl px-4 py-8">
      {/* Header row */}
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">Admin Dashboard</h1>
        <LiveToolbar
          updatedAt={activeQ.dataUpdatedAt}
          isFetching={activeQ.isFetching}
          onRefresh={refetchActive}
        />
      </div>

      {/* Tab bar */}
      <div className="mb-6 flex gap-1 border-b border-slate-200">
        {tabs.map(t => (
          <button
            key={t.id}
            onClick={() => setTab(t.id)}
            className={`px-4 py-2 text-sm font-medium transition ${
              tab === t.id
                ? 'border-b-2 border-indigo-600 text-indigo-700'
                : 'text-slate-500 hover:text-slate-800'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'overview' && (
        <OverviewTab stats={statsQ.data} loading={statsQ.isLoading} error={statsQ.error} onNavigate={setTab} />
      )}
      {tab === 'users' && (
        <UsersTab
          users={usersQ.data}
          loading={usersQ.isLoading}
          error={usersQ.error}
          onToggle={(id, enabled) => toggleUser.mutate({ id, enabled })}
          toggling={toggleUser.isPending}
        />
      )}
      {tab === 'photographers' && (
        <PhotographersTab photographers={photosQ.data} loading={photosQ.isLoading} error={photosQ.error} />
      )}
      {tab === 'inquiries' && (
        <InquiriesTab inquiries={inquiriesQ.data} loading={inquiriesQ.isLoading} error={inquiriesQ.error} />
      )}
      {tab === 'content' && (
        <ContentTab
          items={contentQ.data}
          loading={contentQ.isLoading}
          error={contentQ.error}
          onDelete={(id) => deleteContent.mutate(id)}
          deleting={deleteContent.isPending}
        />
      )}
    </div>
  );
}

// ── Live toolbar ──────────────────────────────────────────────────────────────

function LiveToolbar({ updatedAt, isFetching, onRefresh }: {
  updatedAt: number;
  isFetching: boolean;
  onRefresh: () => void;
}) {
  const timeStr = updatedAt
    ? new Date(updatedAt).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    : '—';

  return (
    <div className="flex items-center gap-3">
      {/* Live pulse dot */}
      <span className="flex items-center gap-1.5 text-xs text-slate-500">
        <span className="relative flex h-2 w-2">
          {isFetching ? (
            <>
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-indigo-400 opacity-75" />
              <span className="relative inline-flex h-2 w-2 rounded-full bg-indigo-500" />
            </>
          ) : (
            <span className="relative inline-flex h-2 w-2 rounded-full bg-emerald-500" />
          )}
        </span>
        {isFetching ? 'Updating…' : `Updated ${timeStr}`}
      </span>

      {/* Manual refresh button */}
      <button
        onClick={onRefresh}
        disabled={isFetching}
        className="flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-medium text-slate-600 shadow-sm transition hover:bg-slate-50 disabled:opacity-50"
      >
        <svg
          viewBox="0 0 16 16"
          fill="none"
          stroke="currentColor"
          strokeWidth="1.5"
          className={`h-3.5 w-3.5 ${isFetching ? 'animate-spin' : ''}`}
        >
          <path strokeLinecap="round" strokeLinejoin="round"
            d="M13.5 8A5.5 5.5 0 1 1 8 2.5M13.5 2.5v3h-3" />
        </svg>
        Refresh
      </button>
    </div>
  );
}

// ── Overview ──────────────────────────────────────────────────────────────────

function OverviewTab({ stats, loading, error, onNavigate }: {
  stats: AdminStats | undefined;
  loading: boolean;
  error: unknown;
  onNavigate: (tab: Tab) => void;
}) {
  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorBanner message={errorMessage(error)} />;
  if (!stats) return null;

  const cards: { label: string; value: number; color: string; tab: Tab }[] = [
    { label: 'Total Users',     value: stats.totalUsers,                color: 'indigo',  tab: 'users' },
    { label: 'Photographers',   value: stats.photographers,             color: 'violet',  tab: 'photographers' },
    { label: 'Customers',       value: stats.customers,                 color: 'fuchsia', tab: 'users' },
    { label: 'Inquiries',       value: stats.totalInquiries,            color: 'amber',   tab: 'inquiries' },
    { label: 'Active Profiles', value: stats.totalPhotographerProfiles, color: 'emerald', tab: 'photographers' },
  ];

  const colorMap: Record<string, string> = {
    indigo:  'bg-indigo-50 text-indigo-700 ring-indigo-200 hover:bg-indigo-100',
    violet:  'bg-violet-50 text-violet-700 ring-violet-200 hover:bg-violet-100',
    fuchsia: 'bg-fuchsia-50 text-fuchsia-700 ring-fuchsia-200 hover:bg-fuchsia-100',
    amber:   'bg-amber-50 text-amber-700 ring-amber-200 hover:bg-amber-100',
    emerald: 'bg-emerald-50 text-emerald-700 ring-emerald-200 hover:bg-emerald-100',
  };

  return (
    <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-5">
      {cards.map(c => (
        <button
          key={c.label}
          onClick={() => onNavigate(c.tab)}
          className={`flex flex-col items-center justify-center rounded-xl p-6 ring-1 transition cursor-pointer ${colorMap[c.color]}`}
        >
          <span className="text-4xl font-bold">{c.value.toLocaleString()}</span>
          <span className="mt-1 text-xs font-medium uppercase tracking-wide">{c.label}</span>
          <span className="mt-2 text-xs opacity-60">View all →</span>
        </button>
      ))}
    </div>
  );
}

// ── Users ─────────────────────────────────────────────────────────────────────

function UsersTab({ users, loading, error, onToggle, toggling }: {
  users: AdminUser[] | undefined;
  loading: boolean;
  error: unknown;
  onToggle: (id: string, enabled: boolean) => void;
  toggling: boolean;
}) {
  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorBanner message={errorMessage(error)} />;
  if (!users) return null;

  return (
    <div className="overflow-hidden rounded-xl border border-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
          <tr>
            <th className="px-4 py-3">Email / Phone</th>
            <th className="px-4 py-3">Role</th>
            <th className="px-4 py-3">Joined</th>
            <th className="px-4 py-3">Status</th>
            <th className="px-4 py-3">Action</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {users.map(u => (
            <tr key={u.id} className="hover:bg-slate-50">
              <td className="px-4 py-3 text-slate-700">{u.email ?? u.phone ?? '—'}</td>
              <td className="px-4 py-3"><RoleBadge role={u.role} /></td>
              <td className="px-4 py-3 text-slate-500">{fmtDate(u.createdAt)}</td>
              <td className="px-4 py-3">
                <span className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ${u.enabled ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-600'}`}>
                  <span className={`h-1.5 w-1.5 rounded-full ${u.enabled ? 'bg-emerald-500' : 'bg-red-500'}`} />
                  {u.enabled ? 'Active' : 'Disabled'}
                </span>
              </td>
              <td className="px-4 py-3">
                <button
                  disabled={toggling}
                  onClick={() => onToggle(u.id, !u.enabled)}
                  className={`rounded px-2.5 py-1 text-xs font-medium transition disabled:opacity-50 ${
                    u.enabled ? 'bg-red-50 text-red-600 hover:bg-red-100' : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
                  }`}
                >
                  {u.enabled ? 'Disable' : 'Enable'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {users.length === 0 && <EmptyState message="No users found." />}
    </div>
  );
}

// ── Photographers ─────────────────────────────────────────────────────────────

function PhotographersTab({ photographers, loading, error }: {
  photographers: PhotographerProfile[] | undefined;
  loading: boolean;
  error: unknown;
}) {
  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorBanner message={errorMessage(error)} />;
  if (!photographers) return null;

  return (
    <div className="overflow-hidden rounded-xl border border-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
          <tr>
            <th className="px-4 py-3">Name</th>
            <th className="px-4 py-3">Location</th>
            <th className="px-4 py-3">Experience</th>
            <th className="px-4 py-3">Rate/hr</th>
            <th className="px-4 py-3">Status</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {photographers.map(p => (
            <tr key={p.id} className="hover:bg-slate-50">
              <td className="px-4 py-3 font-medium text-slate-800">{p.displayName}</td>
              <td className="px-4 py-3 text-slate-500">{p.location}</td>
              <td className="px-4 py-3 text-slate-500">{p.yearsOfExperience}y</td>
              <td className="px-4 py-3 text-slate-500">${p.pricePerHour}/hr</td>
              <td className="px-4 py-3">
                <span className={`inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-xs font-medium ${p.available ? 'bg-emerald-50 text-emerald-700' : 'bg-slate-100 text-slate-500'}`}>
                  <span className={`h-1.5 w-1.5 rounded-full ${p.available ? 'bg-emerald-500' : 'bg-slate-400'}`} />
                  {p.available ? 'Available' : 'Unavailable'}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {photographers.length === 0 && <EmptyState message="No photographer profiles yet." />}
    </div>
  );
}

// ── Inquiries ─────────────────────────────────────────────────────────────────

function InquiriesTab({ inquiries, loading, error }: {
  inquiries: Inquiry[] | undefined;
  loading: boolean;
  error: unknown;
}) {
  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorBanner message={errorMessage(error)} />;
  if (!inquiries) return null;

  return (
    <div className="overflow-hidden rounded-xl border border-slate-200">
      <table className="w-full text-sm">
        <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
          <tr>
            <th className="px-4 py-3">Event</th>
            <th className="px-4 py-3">Date</th>
            <th className="px-4 py-3">Location</th>
            <th className="px-4 py-3">Budget</th>
            <th className="px-4 py-3">Status</th>
            <th className="px-4 py-3">Sent</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-100">
          {inquiries.map(inq => (
            <tr key={inq.id} className="hover:bg-slate-50">
              <td className="px-4 py-3 font-medium text-slate-800">{inq.eventType}</td>
              <td className="px-4 py-3 text-slate-500">{inq.eventDate}</td>
              <td className="px-4 py-3 text-slate-500">{inq.location ?? '—'}</td>
              <td className="px-4 py-3 text-slate-500">{inq.budget ? `$${inq.budget}` : '—'}</td>
              <td className="px-4 py-3"><InquiryStatusBadge status={inq.status} /></td>
              <td className="px-4 py-3 text-slate-500">{fmtDate(inq.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {inquiries.length === 0 && <EmptyState message="No inquiries yet." />}
    </div>
  );
}

// ── Content ───────────────────────────────────────────────────────────────────

type SortDir = 'asc' | 'desc';

function ContentTab({ items, loading, error, onDelete, deleting }: {
  items: AdminContentItem[] | undefined;
  loading: boolean;
  error: unknown;
  onDelete: (id: string) => void;
  deleting: boolean;
}) {
  const [sortDir, setSortDir] = useState<SortDir>('desc');
  const [confirmId, setConfirmId] = useState<string | null>(null);

  const sorted = useMemo(() => {
    if (!items) return [];
    return [...items].sort((a, b) =>
      sortDir === 'desc' ? b.sizeBytes - a.sizeBytes : a.sizeBytes - b.sizeBytes
    );
  }, [items, sortDir]);

  const totalBytes = useMemo(() => items?.reduce((s, i) => s + i.sizeBytes, 0) ?? 0, [items]);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorBanner message={errorMessage(error)} />;
  if (!items) return null;

  const handleConfirm = () => {
    if (confirmId) { onDelete(confirmId); setConfirmId(null); }
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl bg-slate-50 px-5 py-3 ring-1 ring-slate-200">
        <div>
          <span className="text-sm text-slate-500">Total storage used </span>
          <span className="font-semibold text-slate-900">{fmtBytes(totalBytes)}</span>
          <span className="ml-3 text-sm text-slate-400">{items.length} file{items.length !== 1 ? 's' : ''}</span>
        </div>
        <button
          onClick={() => setSortDir(d => d === 'desc' ? 'asc' : 'desc')}
          className="flex items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-xs font-medium text-slate-600 shadow-sm hover:bg-slate-50"
        >
          Sort by size <SortIcon dir={sortDir} />
        </button>
      </div>

      <div className="overflow-hidden rounded-xl border border-slate-200">
        <table className="w-full text-sm">
          <thead className="bg-slate-50 text-left text-xs font-semibold uppercase tracking-wide text-slate-500">
            <tr>
              <th className="px-4 py-3">File</th>
              <th className="px-4 py-3">Photographer</th>
              <th className="px-4 py-3">Type</th>
              <th className="px-4 py-3">
                <button onClick={() => setSortDir(d => d === 'desc' ? 'asc' : 'desc')}
                  className="flex items-center gap-1 hover:text-slate-800">
                  Size <SortIcon dir={sortDir} />
                </button>
              </th>
              <th className="px-4 py-3">Uploaded</th>
              <th className="px-4 py-3">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {sorted.map(item => (
              <tr key={item.id} className="hover:bg-slate-50">
                <td className="px-4 py-3">
                  <div className="flex items-center gap-3">
                    <MediaThumb item={item} />
                    <span className="max-w-[180px] truncate text-slate-700" title={item.originalFileName ?? undefined}>
                      {item.originalFileName ?? '—'}
                    </span>
                  </div>
                </td>
                <td className="px-4 py-3 text-slate-600">{item.photographerDisplayName}</td>
                <td className="px-4 py-3"><MediaTypeBadge type={item.mediaType} /></td>
                <td className="px-4 py-3 font-mono text-slate-700">
                  <SizeBar bytes={item.sizeBytes} maxBytes={sorted[0]?.sizeBytes ?? 1} />
                </td>
                <td className="px-4 py-3 text-slate-500">{fmtDate(item.uploadedAt)}</td>
                <td className="px-4 py-3">
                  {confirmId === item.id ? (
                    <span className="flex items-center gap-1">
                      <button disabled={deleting} onClick={handleConfirm}
                        className="rounded px-2 py-0.5 text-xs font-medium bg-red-600 text-white hover:bg-red-700 disabled:opacity-50">
                        Confirm
                      </button>
                      <button onClick={() => setConfirmId(null)}
                        className="rounded px-2 py-0.5 text-xs font-medium bg-slate-100 text-slate-600 hover:bg-slate-200">
                        Cancel
                      </button>
                    </span>
                  ) : (
                    <button onClick={() => setConfirmId(item.id)}
                      className="rounded px-2.5 py-1 text-xs font-medium text-red-600 bg-red-50 hover:bg-red-100 transition">
                      Delete
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {sorted.length === 0 && <EmptyState message="No content uploaded yet." />}
      </div>
    </div>
  );
}

// ── Content sub-components ────────────────────────────────────────────────────

function MediaThumb({ item }: { item: AdminContentItem }) {
  return (
    <div className="h-10 w-10 shrink-0 overflow-hidden rounded bg-slate-100">
      {item.mediaType === 'IMAGE' ? (
        <img src={item.publicUrl} alt="" className="h-full w-full object-cover" loading="lazy" />
      ) : (
        <div className="flex h-full w-full items-center justify-center text-slate-400">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" className="h-5 w-5">
            <path strokeLinecap="round" strokeLinejoin="round"
              d="m15.75 10.5 4.72-4.72a.75.75 0 0 1 1.28.53v11.38a.75.75 0 0 1-1.28.53l-4.72-4.72M4.5 18.75h9a2.25 2.25 0 0 0 2.25-2.25v-9a2.25 2.25 0 0 0-2.25-2.25h-9A2.25 2.25 0 0 0 2.25 7.5v9a2.25 2.25 0 0 0 2.25 2.25Z" />
          </svg>
        </div>
      )}
    </div>
  );
}

function SizeBar({ bytes, maxBytes }: { bytes: number; maxBytes: number }) {
  const pct = maxBytes > 0 ? Math.round((bytes / maxBytes) * 100) : 0;
  return (
    <div className="flex items-center gap-2">
      <span className="w-16 text-right text-xs">{fmtBytes(bytes)}</span>
      <div className="h-1.5 w-20 rounded-full bg-slate-100">
        <div className="h-full rounded-full bg-indigo-400" style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
}

function SortIcon({ dir }: { dir: SortDir }) {
  return (
    <svg viewBox="0 0 16 16" fill="currentColor" className="h-3 w-3">
      {dir === 'desc' ? <path d="M8 12L3 6h10l-5 6Z" /> : <path d="M8 4l5 6H3L8 4Z" />}
    </svg>
  );
}

function MediaTypeBadge({ type }: { type: string }) {
  const map: Record<string, string> = {
    IMAGE: 'bg-sky-50 text-sky-700',
    VIDEO: 'bg-violet-50 text-violet-700',
    REEL:  'bg-pink-50 text-pink-700',
  };
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${map[type] ?? 'bg-slate-100 text-slate-600'}`}>
      {type}
    </span>
  );
}

// ── Shared UI helpers ─────────────────────────────────────────────────────────

function RoleBadge({ role }: { role: string }) {
  const map: Record<string, string> = {
    PHOTOGRAPHER: 'bg-violet-50 text-violet-700',
    CUSTOMER:     'bg-indigo-50 text-indigo-700',
    ADMIN:        'bg-amber-50 text-amber-700',
  };
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${map[role] ?? 'bg-slate-100 text-slate-600'}`}>
      {role}
    </span>
  );
}

function InquiryStatusBadge({ status }: { status: string }) {
  const map: Record<string, string> = {
    NEW:       'bg-blue-50 text-blue-700',
    READ:      'bg-slate-100 text-slate-600',
    RESPONDED: 'bg-emerald-50 text-emerald-700',
    CLOSED:    'bg-red-50 text-red-600',
  };
  return (
    <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${map[status] ?? 'bg-slate-100 text-slate-600'}`}>
      {status}
    </span>
  );
}

function LoadingSpinner() {
  return (
    <div className="flex items-center justify-center py-16">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-indigo-200 border-t-indigo-600" />
    </div>
  );
}

function ErrorBanner({ message }: { message: string }) {
  return (
    <div className="rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 ring-1 ring-red-200">
      {message}
    </div>
  );
}

function EmptyState({ message }: { message: string }) {
  return <p className="px-4 py-8 text-center text-sm text-slate-400">{message}</p>;
}

function fmtDate(iso: string): string {
  return new Date(iso).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
}

function fmtBytes(bytes: number): string {
  if (bytes < 1024)       return `${bytes} B`;
  if (bytes < 1024 ** 2)  return `${(bytes / 1024).toFixed(1)} KB`;
  if (bytes < 1024 ** 3)  return `${(bytes / 1024 ** 2).toFixed(1)} MB`;
  return `${(bytes / 1024 ** 3).toFixed(2)} GB`;
}
