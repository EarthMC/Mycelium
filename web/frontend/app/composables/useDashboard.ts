import { createSharedComposable } from "@vueuse/core";

const _useDashboard = () => {
  const router = useRouter();

  defineShortcuts({
    "g-h": () => router.push("/"),
    "g-p": () => router.push("/players"),
    "g-s": () => router.push("/servers"),
  });

  return {
  };
};

export const useDashboard = createSharedComposable(_useDashboard);
