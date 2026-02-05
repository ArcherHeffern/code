import "@radix-ui/themes/styles.css";
import { Theme, ThemePanel } from "@radix-ui/themes";
import TrainTracker from "./components/trainTracker";

function App() {

  return (
    <Theme accentColor="crimson" grayColor="sand" radius="large" scaling="95%">
      <TrainTracker />
      <ThemePanel />
    </Theme>
  )
}

export default App
