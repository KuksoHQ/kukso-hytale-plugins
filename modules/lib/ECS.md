# Hytale ECS Implementation Guide

## 1. Core Philosophy: Composition Over Inheritance
**Constraint:** Rigid Object-Oriented Programming (OOP) hierarchies are **forbidden** for game entities.
* **The Shift:** Entities are defined by what they *have* (Components), not what they *are* (Classes).
* **Wrong:** `public class Zombie extends Monster`
* **Right:** `EntityRef` (ID: 5042) + `ZombieComponent` + `AiComponent`

### Separation of Concerns
1.  **Entities:** Unique Identifiers (IDs) only. Containers for components.
2.  **Components:** Pure Data (DTOs). No logic, no methods.
3.  **Systems:** Pure Logic. Process entities based on the components they possess.

---

## 2. Architecture Hierarchy

| Object | Scope | Description |
| :--- | :--- | :--- |
| **Universe** | Singleton | The root. Contains one or more Worlds. Access via `Universe.get()`. |
| **World** | Instance | Represents a specific map/dimension. Contains the `EntityStore`. |
| **EntityStore** | Database | The central hub. All ECS operations (Add/Remove/Get) happen here. |
| **Ref** | Handle | A safe pointer to an entity ID. |
| **PlayerRef** | Network | A bridge connecting a client connection to an ECS entity. |

---

## 3. Implementation Patterns

### A. Defining Components
**Rule:** Components must be POJOs (Plain Old Java Objects).
**Performance Requirement:** Always include a static `ComponentType` field for O(1) lookups.

```java
public class ManaComponent implements Component {
    // CRITICAL: Static index for performance
    public static final ComponentType<ManaComponent> TYPE = ComponentType.create(ManaComponent.class);

    public float current;
    public float max;

    public ManaComponent(float max) {
        this.max = max;
        this.current = max;
    }
}
```

### B. The "Read" Transaction
Pattern: Ref + Store + ComponentType = Data

```java
// 1. Context: You usually have an EntityRef and a World
EntityStore store = world.getEntityStore();

// 2. Fetch Data using the static TYPE
ManaComponent mana = store.get(entityRef, ManaComponent.TYPE);

// 3. Null Check (Safe Access)
if (mana != null) {
    boolean canCast = mana.current > 10;
}
```

### C. The "Write" Transaction
Rule: Modifications are explicit transactions on the EntityStore.

Modify (Update value):
```java
store.modify(entityRef, ManaComponent.TYPE, m -> {
    m.current -= 10;
});
Add (New capability):
```

```java
store.add(entityRef, new BurningComponent(5.0f));
Remove (Strip capability):
```

```java
store.remove(entityRef, InvisibilityComponent.TYPE);
```

### D. Systems & Logic
Logic is encapsulated in Systems. Choose the correct base class based on timing requirements.

#### System Types
| System Class            | Execution Timing    | Use Case |
|:------------------------|:--------------------| :--- |
| EntityTickingSystem     | Every tick (Frame)  | Movement, AI, Regen, Cooldowns. |
| RefSystem | On Add/Remove       | Lifecycle hooks (Spawn sounds, Despawn cleanup). |
| RefChangeSystem         | On Component Change | Reactive logic (e.g., Update UI when Health changes). |
| DamageEventSystem       | On Damage Event     | Combat calculations, Armor reduction. |

#### Example: Regeneration System (EntityTickingSystem)
```java
public class ManaRegenSystem extends EntityTickingSystem {
    
    // 1. Filter: Select entities with Mana that are NOT Dead
    @Override
    protected Query getQuery() {
        return Query.select(ManaComponent.TYPE).exclude(DeadComponent.TYPE);
    }

    // 2. Process: Runs every tick for matching entities
    @Override
    protected void tick(EntityRef ref, float dt) {
        EntityStore store = getStore();
        
        store.modify(ref, ManaComponent.TYPE, mana -> {
            if (mana.current < mana.max) {
                // Use 'dt' (Delta Time) for frame-rate independence
                mana.current += 1.5f * dt; 
            }
        });
    }
}
```

### E. Advanced Concepts
#### Queries (Selectors)
Queries act as SQL SELECT statements for entities.

AND: Query.select(TypeA, TypeB) (Must have both)
OR: Query.any(TypeA, TypeB) (Must have at least one)
NOT: Query.exclude(TypeA) (Must not have)

#### Archetypes (Memory Layout)
Concept: Entities with the exact same set of components share an "Archetype".
Performance: Entities of the same Archetype are stored contiguously in memory (Structure of Arrays).
Optimization: Avoid adding/removing components frequently (e.g., every frame) as this forces memory moves. Use flags or values inside existing components instead.

#### Interactions vs. ECS
Interactions: Client-side handling of user input timing (Animations, Clicks).
ECS: Server-side handling of logic results.
Flow: User Clicks -> Interaction Plays Animation -> Hit Point -> Interaction adds DamageEvent -> ECS System processes Damage.

### F. Glossary
`dt` (Delta Time): Time in seconds since the last frame. Essential for math to be frame-rate independent (speed * dt).
`Ref`: A safe handle to an entity. Always check ref.isValid() if holding a ref over time.
`ComponentType`: The unique integer ID for a component class. Always use this instead of Class.class.