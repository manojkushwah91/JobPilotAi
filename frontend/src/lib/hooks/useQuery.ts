import { useQuery, useMutation, useQueryClient, type UseQueryOptions, type UseMutationOptions } from '@tanstack/react-query';
import { apiGet, apiPost, apiPut, apiDelete } from '@/lib/api/client';
import type { ApiResponse } from '@/types';

export function useApiQuery<T>(
  key: string[],
  url: string,
  params?: Record<string, unknown>,
  options?: Omit<UseQueryOptions<ApiResponse<T>>, 'queryKey' | 'queryFn'>
) {
  return useQuery({
    queryKey: [...key, params],
    queryFn: () => apiGet<T>(url, params),
    ...options,
  });
}

export function useApiMutation<T, V = unknown>(
  method: 'POST' | 'PUT' | 'DELETE',
  url: string,
  options?: UseMutationOptions<ApiResponse<T>, Error, V>
) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (variables: V) => {
      switch (method) {
        case 'POST': return apiPost<T>(url, variables);
        case 'PUT': return apiPut<T>(url, variables);
        case 'DELETE': return apiDelete<T>(url);
      }
    },
    onSuccess: () => queryClient.invalidateQueries(),
    ...options,
  });
}
